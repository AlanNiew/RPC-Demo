package com.alan.rpc.v4.demo;

import com.alan.rpc.v4.registry.RegistryServer;

import java.io.IOException;

/**
 * 注册中心启动类
 * v4 版本：与 v3 保持一致
 */
public class RegistryMain {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 启动注册中心
        RegistryServer registryServer = new RegistryServer(9000);
        registryServer.start();

        System.out.println("[注册中心] 按任意键退出...");
        System.in.read();
    }
}
