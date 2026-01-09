package com.alan.rpc.v2.demo;

import com.alan.rpc.v2.consumer.RpcClient;

/**
 * 客户端启动类 - v2 版本（动态代理）
 */
public class ClientMain {
    public static void main(String[] args) {
        // 创建 RPC 客户端
        RpcClient rpcClient = new RpcClient("127.0.0.1", 8080);

        System.out.println("==================== v2 客户端启动 ====================");
        System.out.println("[客户端] 使用动态代理调用远程服务\n");

        // 创建代理对象 - 像本地对象一样使用
        UserService userService = rpcClient.getProxy(UserService.class);

        // 调用 getUserName 方法
        String userName = userService.getUserName(1001);
        System.out.println("[客户端] userService.getUserName(1001) => " + userName);

        // 调用 createUser 方法
        Boolean result = userService.createUser("李四", 30);
        System.out.println("[客户端] userService.createUser(\"李四\", 30) => " + result);

        // 调用 getUserInfo 方法
        String userInfo = userService.getUserInfo(1001);
        System.out.println("[客户端] userService.getUserInfo(1001) => " + userInfo);

        System.out.println("\n[客户端] v2 动态代理版本，调用像本地方法一样自然！");
    }
}
