package com.alan.rpc.v4.demo;

import com.alan.rpc.v4.common.SerializationTypeEnum;
import com.alan.rpc.v4.provider.RpcServer;

/**
 * RPC 服务端启动类 - v4 版本支持多序列化方式
 *
 * 使用方式：
 * 1. 默认使用 Java 序列化
 * 2. 可指定序列化类型：SerializationTypeEnum.JSON / HESSIAN / KRYO
 */
public class ServerMain {
    public static void main(String[] args) throws InterruptedException {
        // 选择序列化类型：可修改为 JSON、HESSIAN、KRYO 进行测试
        SerializationTypeEnum serializationType = SerializationTypeEnum.JSON;

        // 创建服务端（指定序列化类型）
        RpcServer rpcServer = new RpcServer(8080, "127.0.0.1", 9000, serializationType);

        // 注册服务实现
        rpcServer.registerService(UserService.class, new UserServiceImpl());

        System.out.println("========================================");
        System.out.println("v4 RPC 服务端启动");
        System.out.println("序列化方式: " + serializationType.getDescription());
        System.out.println("服务端口: 8080");
        System.out.println("注册中心: 127.0.0.1:9000");
        System.out.println("========================================");

        // 启动服务
        rpcServer.start();
    }
}
