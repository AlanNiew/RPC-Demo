package com.alan.rpc.v3.demo;

import com.alan.rpc.v3.provider.RpcServer;

/**
 * 服务端启动类 - v3 版本（支持服务注册）
 */
public class ServerMain {
    public static void main(String[] args) {
        // 创建 RPC 服务器，指定注册中心地址
        RpcServer rpcServer = new RpcServer(8080, "127.0.0.1", 9000);

        // 注册本地服务
        rpcServer.registerService(UserService.class, new UserServiceImpl());

        // 启动服务器（会自动注册到注册中心）
        System.out.println("==================== v3 服务端启动 ====================");
        rpcServer.start();
    }
}
