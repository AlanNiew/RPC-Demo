package com.alan.rpc.v1.provider;

import com.alan.rpc.v1.common.RpcRequest;
import com.alan.rpc.v1.common.RpcResponse;
import com.alan.rpc.v1.common.Serializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC 服务提供者
 * 负责接收客户端请求，调用本地服务，并返回结果
 */
public class RpcServer {

    /**
     * 服务端口
     */
    private final int port;

    /**
     * 服务注册表：接口名称 -> 服务实例
     */
    private final Map<String, Object> serviceRegistry = new HashMap<>();

    public RpcServer(int port) {
        this.port = port;
    }

    /**
     * 注册服务
     *
     * @param interfaceClass 接口类
     * @param impl           服务实现实例
     */
    public <T> void registerService(Class<T> interfaceClass, T impl) {
        String interfaceName = interfaceClass.getName();
        serviceRegistry.put(interfaceName, impl);
        System.out.println("[服务提供者] 注册服务: " + interfaceName);
    }

    /**
     * 启动 RPC 服务器
     */
    public void start() {
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

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("[服务提供者] 启动成功，监听端口: " + port);

            // 等待服务器 socket 关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.err.println("[服务提供者] 启动失败: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 处理 RPC 请求
     *
     * @param request RPC 请求对象
     * @return RPC 响应对象
     */
    public RpcResponse handleRequest(RpcRequest request) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            // 从服务注册表中获取服务实例
            Object service = serviceRegistry.get(request.getInterfaceName());
            if (service == null) {
                throw new RuntimeException("服务未找到: " + request.getInterfaceName());
            }

            // 使用反射调用方法
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

    /**
     * Netty 服务端处理器
     */
    @ChannelHandler.Sharable
    private class RpcServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // Netty 传递的是 ByteBuf，需要转换为 byte[]
            io.netty.buffer.ByteBuf buf = (io.netty.buffer.ByteBuf) msg;
            try {
                // 读取字节数组
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);

                // 反序列化请求
                RpcRequest request = (RpcRequest) Serializer.deserialize(data);

                // 处理请求
                RpcResponse response = handleRequest(request);

                // 序列化响应并发送
                byte[] responseData = Serializer.serialize(response);
                io.netty.buffer.ByteBuf responseBuf = io.netty.buffer.Unpooled.buffer(responseData.length);
                responseBuf.writeBytes(responseData);
                ctx.writeAndFlush(responseBuf);

                System.out.println("[服务提供者] 处理请求完成: " + request.getRequestId());
            } catch (Exception e) {
                System.err.println("[服务提供者] 处理请求异常: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // 释放 ByteBuf
                buf.release();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public static void main(String[] args) {
        RpcServer server = new RpcServer(8080);
        server.start();
    }
}
