package com.alan.rpc.v3.demo;

import com.alan.rpc.v3.registry.RegistryServer;

/**
 * 注册中心启动类
 */
public class RegistryMain {
    public static void main(String[] args) {
        RegistryServer registryServer = new RegistryServer(9000);
        System.out.println("==================== 注册中心启动 ====================");
        registryServer.start();
    }
}
