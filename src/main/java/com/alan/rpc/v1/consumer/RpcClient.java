package com.alan.rpc.v1.consumer;

import com.alan.rpc.v1.common.RpcRequest;
import com.alan.rpc.v1.common.RpcResponse;
import com.alan.rpc.v1.common.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * RPC 客户端
 * 负责向服务端发起 RPC 调用请求
 */
public class RpcClient {

    /**
     * 服务端地址
     */
    private final String host;

    /**
     * 服务端端口
     */
    private final int port;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 发起 RPC 调用
     *
     * @param interfaceName 接口名称
     * @param methodName    方法名称
     * @param parameterTypes 参数类型
     * @param parameters    参数值
     * @return 调用结果
     */
    public Object invoke(String interfaceName, String methodName,
                         Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        // 构建请求对象
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(interfaceName);
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(parameters);

        // 序列化请求
        byte[] requestBytes = Serializer.serialize(request);

        // 发送请求并获取响应
        byte[] responseBytes = sendRequest(requestBytes);

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
    private byte[] sendRequest(byte[] requestBytes) throws Exception {
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

            // 连接服务端
            ChannelFuture future = bootstrap.connect(host, port).sync();

            // 设置回调
            handler.setLatch(latch);

            // 发送请求 - 将 byte[] 包装成 ByteBuf
            io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer(requestBytes.length);
            buffer.writeBytes(requestBytes);
            future.channel().writeAndFlush(buffer);

            // 等待响应
            latch.await();

            // 获取响应数据
            byte[] responseBytes = handler.getResponse();

            // 关闭连接
            future.channel().close().sync();

            return responseBytes;
        } finally {
            group.shutdownGracefully();
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
            // Netty 传递的是 ByteBuf，需要转换为 byte[]
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
