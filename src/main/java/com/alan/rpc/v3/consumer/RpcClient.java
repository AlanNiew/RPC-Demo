package com.alan.rpc.v3.consumer;

import com.alan.rpc.v3.common.RpcRequest;
import com.alan.rpc.v3.common.RpcResponse;
import com.alan.rpc.v3.common.Serializer;
import com.alan.rpc.v3.registry.RegistryClient;
import com.alan.rpc.v3.registry.ServiceInstance;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * RPC 客户端 - 支持服务发现
 */
public class RpcClient {

    private final String registryHost;
    private final int registryPort;
    private final RegistryClient registryClient;

    public RpcClient(String registryHost, int registryPort) {
        this.registryHost = registryHost;
        this.registryPort = registryPort;
        this.registryClient = new RegistryClient(registryHost, registryPort);
    }

    /**
     * 创建服务接口的代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvocationHandler(interfaceClass.getName())
        );
    }

    /**
     * 发起 RPC 调用
     */
    private Object invoke(String interfaceName, String methodName,
                         Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        // 从注册中心发现服务
        List<ServiceInstance> instances = registryClient.discover(interfaceName);
        if (instances.isEmpty()) {
            throw new RuntimeException("没有可用的服务实例: " + interfaceName);
        }

        // 简单的负载均衡：取第一个可用实例
        // v5 版本将实现更复杂的负载均衡策略
        ServiceInstance instance = instances.get(0);
        System.out.println("[客户端] 选择服务实例: " + instance.getAddress());

        // 构建请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(interfaceName);
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(parameters);

        // 序列化请求
        byte[] requestBytes = Serializer.serialize(request);

        // 发送请求并获取响应
        byte[] responseBytes = sendRequest(instance.getHost(), instance.getPort(), requestBytes);

        // 反序列化响应
        RpcResponse response = (RpcResponse) Serializer.deserialize(responseBytes);

        if (response.getException() != null) {
            throw response.getException();
        }

        return response.getResult();
    }

    /**
     * 发送请求到服务端
     */
    private byte[] sendRequest(String host, int port, byte[] requestBytes) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        RpcClientHandler handler = new RpcClientHandler();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(handler);
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            handler.setLatch(latch);

            io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer(requestBytes.length);
            buffer.writeBytes(requestBytes);
            future.channel().writeAndFlush(buffer);

            latch.await();

            byte[] responseBytes = handler.getResponse();
            future.channel().close().sync();

            return responseBytes;
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * JDK 动态代理调用处理器
     */
    private class RpcInvocationHandler implements InvocationHandler {

        private final String interfaceName;

        public RpcInvocationHandler(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return RpcClient.this.invoke(
                    interfaceName,
                    method.getName(),
                    method.getParameterTypes(),
                    args
            );
        }
    }

    /**
     * Netty 客户端处理器
     */
    private static class RpcClientHandler extends ChannelInboundHandlerAdapter {

        private byte[] response;
        private CountDownLatch latch;

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        public byte[] getResponse() {
            return response;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            io.netty.buffer.ByteBuf buf = (io.netty.buffer.ByteBuf) msg;
            try {
                response = new byte[buf.readableBytes()];
                buf.readBytes(response);
            } finally {
                buf.release();
            }
            latch.countDown();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
            latch.countDown();
        }
    }
}
