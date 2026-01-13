package com.alan.rpc.v3.provider;

import com.alan.rpc.v3.common.RpcRequest;
import com.alan.rpc.v3.common.RpcResponse;
import com.alan.rpc.v3.common.Serializer;
import com.alan.rpc.v3.registry.RegistryClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RPC 服务提供者 - 支持服务注册
 */
public class RpcServer {

    private final int port;
    private final String registryHost;
    private final int registryPort;
    private final Map<String, Object> serviceRegistry = new HashMap<>();
    private final RegistryClient registryClient;
    private final ScheduledExecutorService heartbeatExecutor;

    public RpcServer(int port, String registryHost, int registryPort) {
        this.port = port;
        this.registryHost = registryHost;
        this.registryPort = registryPort;
        this.registryClient = new RegistryClient(registryHost, registryPort);
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 注册服务
     */
    public <T> void registerService(Class<T> interfaceClass, T impl) {
        String interfaceName = interfaceClass.getName();
        serviceRegistry.put(interfaceName, impl);
        System.out.println("[服务提供者] 本地注册服务: " + interfaceName);
    }

    /**
     * 启动 RPC 服务器
     */
    public void start() {
        // 启动 Netty 服务器
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new RpcServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("[服务提供者] 启动成功，监听端口: " + port);

            // 注册所有服务到注册中心
            registerToRegistry();

            // 启动心跳
            startHeartbeat();

            // 等待服务器 socket 关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.err.println("[服务提供者] 启动失败: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            heartbeatExecutor.shutdown();
        }
    }

    /**
     * 注册到注册中心
     */
    private void registerToRegistry() {
        String instanceId = UUID.randomUUID().toString();
        String host = "127.0.0.1"; // 实际应该获取本机IP

        for (String serviceName : serviceRegistry.keySet()) {
            registryClient.register(serviceName, host, port, instanceId);
        }

        // 保存实例ID用于心跳
        this.instanceId = instanceId;
        this.serviceNameList = new java.util.ArrayList<>(serviceRegistry.keySet());
    }

    private String instanceId;
    private java.util.List<String> serviceNameList;

    /**
     * 启动心跳
     */
    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            for (String serviceName : serviceNameList) {
                try {
                    registryClient.heartbeat(serviceName, instanceId);
                } catch (Exception e) {
                    System.err.println("[服务提供者] 心跳失败: " + serviceName);
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * 处理 RPC 请求
     */
    public RpcResponse handleRequest(RpcRequest request) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            Object service = serviceRegistry.get(request.getInterfaceName());
            if (service == null) {
                throw new RuntimeException("服务未找到: " + request.getInterfaceName());
            }

            Object result = service.getClass()
                    .getMethod(request.getMethodName(), request.getParameterTypes())
                    .invoke(service, request.getParameters());

            response.setResult(result);
        } catch (Exception e) {
            response.setException(e);
            System.err.println("[服务提供者] 请求处理失败: " + e.getMessage());
        }

        return response;
    }

    @ChannelHandler.Sharable
    private class RpcServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            io.netty.buffer.ByteBuf buf = (io.netty.buffer.ByteBuf) msg;
            try {
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);

                RpcRequest request = (RpcRequest) Serializer.deserialize(data);
                RpcResponse response = handleRequest(request);

                byte[] responseData = Serializer.serialize(response);
                io.netty.buffer.ByteBuf responseBuf = io.netty.buffer.Unpooled.buffer(responseData.length);
                responseBuf.writeBytes(responseData);
                ctx.writeAndFlush(responseBuf);

                System.out.println("[服务提供者] 处理请求完成: " + request.getRequestId());
            } catch (Exception e) {
                System.err.println("[服务提供者] 处理请求异常: " + e.getMessage());
                e.printStackTrace();
            } finally {
                buf.release();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
