package com.alan.rpc.v4.demo;

import com.alan.rpc.v4.common.SerializationTypeEnum;
import com.alan.rpc.v4.consumer.RpcClient;

/**
 * RPC 客户端启动类 - v4 版本支持多序列化方式
 *
 * 使用方式：
 * 1. 客户端和服务端必须使用相同的序列化类型
 * 2. 可指定序列化类型：SerializationTypeEnum.JSON / HESSIAN / KRYO / JAVA
 */
public class ClientMain {
    public static void main(String[] args) {
        // 选择序列化类型：必须与服务端一致
        SerializationTypeEnum serializationType = SerializationTypeEnum.JSON;

        // 创建客户端（指定序列化类型）
        RpcClient rpcClient = new RpcClient("127.0.0.1", 9000, serializationType);

        // 获取服务代理
        UserService userService = rpcClient.getProxy(UserService.class);

        System.out.println("========================================");
        System.out.println("v4 RPC 客户端启动");
        System.out.println("序列化方式: " + serializationType.getDescription());
        System.out.println("注册中心: 127.0.0.1:9000");
        System.out.println("========================================");

        try {
            // 调用远程方法
            System.out.println("\n--- 测试 1: getUserName ---");
            String userName = userService.getUserName(1001);
            System.out.println("调用结果: " + userName);

            System.out.println("\n--- 测试 2: createUser ---");
            Boolean result = userService.createUser("张三", 25);
            System.out.println("调用结果: " + result);

            System.out.println("\n--- 测试 3: getUserInfo ---");
            String userInfo = userService.getUserInfo(1001);
            System.out.println("调用结果: " + userInfo);

            System.out.println("\n========================================");
            System.out.println("所有调用成功完成！");
        } catch (Exception e) {
            System.err.println("调用失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
