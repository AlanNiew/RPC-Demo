package com.alan.rpc.v3.registry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * 注册中心客户端
 * 服务提供者和消费者通过它与注册中心交互
 */
public class RegistryClient {

    /**
     * 注册中心地址
     */
    private final String registryHost;

    /**
     * 注册中心端口
     */
    private final int registryPort;

    public RegistryClient(String registryHost, int registryPort) {
        this.registryHost = registryHost;
        this.registryPort = registryPort;
    }

    /**
     * 注册服务
     */
    public void register(String serviceName, String host, int port, String instanceId) {
        sendRequest("REGISTER", () -> {
            try (Socket socket = new Socket(registryHost, registryPort);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject("REGISTER");
                oos.writeObject(serviceName);
                oos.writeObject(host);
                oos.writeObject(port);
                oos.writeObject(instanceId);
                oos.flush();

                ois.readBoolean();
            }
            return null;
        });
    }

    /**
     * 发现服务
     */
    public List<ServiceInstance> discover(String serviceName) {
        return sendRequest("DISCOVER", () -> {
            try (Socket socket = new Socket(registryHost, registryPort);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject("DISCOVER");
                oos.writeObject(serviceName);
                oos.flush();

                @SuppressWarnings("unchecked")
                List<ServiceInstance> instances = (List<ServiceInstance>) ois.readObject();
                return instances;
            }
        });
    }

    /**
     * 服务下线
     */
    public void deregister(String serviceName, String instanceId) {
        sendRequest("DEREGISTER", () -> {
            try (Socket socket = new Socket(registryHost, registryPort);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject("DEREGISTER");
                oos.writeObject(serviceName);
                oos.writeObject(instanceId);
                oos.flush();

                ois.readBoolean();
            }
            return null;
        });
    }

    /**
     * 发送心跳
     */
    public void heartbeat(String serviceName, String instanceId) {
        sendRequest("HEARTBEAT", () -> {
            try (Socket socket = new Socket(registryHost, registryPort);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject("HEARTBEAT");
                oos.writeObject(serviceName);
                oos.writeObject(instanceId);
                oos.flush();

                ois.readBoolean();
            }
            return null;
        });
    }

    /**
     * 发送请求的通用方法
     */
    private <T> T sendRequest(String type, RequestCallback<T> callback) {
        try {
            return callback.execute();
        } catch (Exception e) {
            System.err.println("[注册中心客户端] " + type + " 请求失败: " + e.getMessage());
            throw new RuntimeException("注册中心通信失败", e);
        }
    }

    @FunctionalInterface
    private interface RequestCallback<T> {
        T execute() throws Exception;
    }
}
