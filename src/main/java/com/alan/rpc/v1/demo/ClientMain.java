package com.alan.rpc.v1.demo;

import com.alan.rpc.v1.consumer.RpcClient;

/**
 * 客户端启动类
 */
public class ClientMain {
    public static void main(String[] args) throws Exception {
        // 创建 RPC 客户端
        RpcClient rpcClient = new RpcClient("127.0.0.1", 8080);

        System.out.println("==================== 客户端启动 ====================");

        // 调用 getUserName 方法
        String userName = (String) rpcClient.invoke(
                UserService.class.getName(),
                "getUserName",
                new Class[]{Integer.class},
                new Object[]{1001}
        );
        System.out.println("[客户端] 调用 getUserName(1001) 结果: " + userName);

        // 调用 createUser 方法
        Boolean result = (Boolean) rpcClient.invoke(
                UserService.class.getName(),
                "createUser",
                new Class[]{String.class, Integer.class},
                new Object[]{"张三", 25}
        );
        System.out.println("[客户端] 调用 createUser(\"张三\", 25) 结果: " + result);
    }
}
