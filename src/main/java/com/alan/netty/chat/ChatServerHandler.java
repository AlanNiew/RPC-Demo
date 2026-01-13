package com.alan.netty.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

// ChatServerHandler.java
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    // 新客户端连接
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String welcome = "【系统】欢迎进入聊天室！当前在线人数: " +
                ChatServer.clients.size() + "\n";
        ctx.writeAndFlush(welcome);
        ChatServer.clients.add(ctx.channel());

        // 广播通知
        broadcast("【系统】" + ctx.channel().remoteAddress() + " 加入聊天室\n");
    }

    // 收到消息
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 格式：地址: 消息
        String from = ctx.channel().remoteAddress().toString();
        String formattedMsg = "【" + from + "】" + msg + "\n";
        broadcast(formattedMsg);
    }

    // 客户端断开
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChatServer.clients.remove(ctx.channel());
        broadcast("【系统】" + ctx.channel().remoteAddress() + " 离开聊天室\n");
    }

    // 异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println(cause.getMessage());
        ctx.close();
    }

    // 广播消息给所有人
    private void broadcast(String msg) {
        ChatServer.clients.writeAndFlush(msg);
    }
}