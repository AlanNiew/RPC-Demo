package com.alan.rpc.v3.demo;

import com.alan.rpc.v3.consumer.RpcClient;

/**
 * 客户端启动类 - v3 版本（支持服务发现）
 */
public class ClientMain {
    public static void main(String[] args) throws Exception {
        // 创建 RPC 客户端，指定注册中心地址
        RpcClient rpcClient = new RpcClient("127.0.0.1", 9000);

        System.out.println("==================== v3 客户端启动 ====================");
        System.out.println("[客户端] 使用注册中心发现服务\n");

        // 等待服务端注册
        Thread.sleep(2000);

        // 创建代理对象
        UserService userService = rpcClient.getProxy(UserService.class);

        // 调用方法
        String userName = userService.getUserName(1001);
        System.out.println("[客户端] userService.getUserName(1001) => " + userName);

        Boolean result = userService.createUser("王五", 28);
        System.out.println("[客户端] userService.createUser(\"王五\", 28) => " + result);

        String userInfo = userService.getUserInfo(1001);
        System.out.println("[客户端] userService.getUserInfo(1001) => " + userInfo);

        System.out.println("\n[客户端] v3 版本：通过注册中心自动发现服务实例！");
    }
}
