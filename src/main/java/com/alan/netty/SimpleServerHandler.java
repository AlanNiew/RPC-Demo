package com.alan.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

// SimpleServerHandler.java
public class SimpleServerHandler extends ChannelInboundHandlerAdapter {

    // 有客户端连接成功时触发
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("客户端连接: " + ctx.channel().remoteAddress());
        ctx.writeAndFlush("欢迎连接到服务器!\n");
    }

    // 收到客户端消息时触发
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // msg默认是ByteBuf类型，是Netty的字节容器
        ByteBuf buf = (ByteBuf) msg;
        try {
            // 将字节转换为字符串
            String received = buf.toString(CharsetUtil.UTF_8);
            System.out.println("收到消息: " + received);

            // 回复客户端
            String response = "服务器回复: " + received;
            ctx.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
        } finally {
            buf.release(); // 重要：释放ByteBuf内存
        }
    }

    // 发生异常时触发
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}