package com.alan.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// ChatClient.java
public class ChatClient {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LineBasedFrameDecoder(1024),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new ChatClientHandler()
                            );
                        }
                    });

            // 连接服务器
            ChannelFuture f = b.connect("localhost", 8888).sync();
            Channel channel = f.channel();

            // 从控制台读取输入
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = reader.readLine();
                if (line == null || "bye".equalsIgnoreCase(line)) {
                    break;
                }
                channel.writeAndFlush(line + "\n");
            }

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

