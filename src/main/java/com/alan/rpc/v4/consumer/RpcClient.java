package com.alan.rpc.v4.consumer;

import com.alan.rpc.v4.common.RpcRequest;
import com.alan.rpc.v4.common.RpcResponse;
import com.alan.rpc.v4.common.SerializationTypeEnum;
import com.alan.rpc.v4.common.serializer.Serializer;
import com.alan.rpc.v4.common.serializer.SerializerFactory;
import com.alan.rpc.v4.registry.RegistryClient;
import com.alan.rpc.v4.registry.ServiceInstance;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
 * RPC 客户端 - v4 版本支持多序列化方式
 */
public class RpcClient {

    private final String registryHost;
    private final int registryPort;
    private final RegistryClient registryClient;
    private final Serializer serializer;

    /**
     * 构造函数 - 指定序列化类型
     *
     * @param registryHost      注册中心主机
     * @param registryPort      注册中心端口
     * @param serializationType 序列化类型
     */
    public RpcClient(String registryHost, int registryPort, SerializationTypeEnum serializationType) {
        this.registryHost = registryHost;
        this.registryPort = registryPort;
        this.serializer = SerializerFactory.getSerializer(serializationType);
        this.registryClient = new RegistryClient(registryHost, registryPort);
        System.out.println("[客户端] 使用序列化方式: " + serializationType.getDescription());
    }

    /**
     * 构造函数 - 使用默认序列化类型（Java 原生序列化）
     *
     * @param registryHost 注册中心主机
     * @param registryPort 注册中心端口
     */
    public RpcClient(String registryHost, int registryPort) {
        this(registryHost, registryPort, SerializationTypeEnum.JAVA);
    }

    /**
     * 构造函数 - 支持字符串 code 指定序列化类型
     *
     * @param registryHost        注册中心主机
     * @param registryPort        注册中心端口
     * @param serializationTypeCode 序列化类型代码（"java", "json", "hessian", "kryo"）
     */
    public RpcClient(String registryHost, int registryPort, String serializationTypeCode) {
        this(registryHost, registryPort, SerializationTypeEnum.fromCode(serializationTypeCode));
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

        // 使用配置的序列化器序列化请求
        byte[] requestBytes = serializer.serialize(request);

        // 发送请求并获取响应
        byte[] responseBytes = sendRequest(instance.getHost(), instance.getPort(), requestBytes);

        // 使用配置的序列化器反序列化响应
        RpcResponse response = serializer.deserialize(responseBytes, RpcResponse.class);

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

            ByteBuf buffer = Unpooled.buffer(requestBytes.length);
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
            ByteBuf buf = (ByteBuf) msg;
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
