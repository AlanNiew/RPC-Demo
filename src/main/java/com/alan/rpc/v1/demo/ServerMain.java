package com.alan.rpc.v1.demo;

import com.alan.rpc.v1.provider.RpcServer;

/**
 * 服务端启动类
 */
public class ServerMain {
    public static void main(String[] args) {
        // 创建 RPC 服务器
        RpcServer rpcServer = new RpcServer(8080);

        // 注册服务
        rpcServer.registerService(UserService.class, new UserServiceImpl());

        // 启动服务器
        System.out.println("==================== 服务端启动 ====================");
        rpcServer.start();
    }
}
