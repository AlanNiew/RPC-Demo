package com.alan.rpc.v4.registry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 注册中心服务器
 * 提供远程服务注册、发现、下线、心跳功能
 */
public class RegistryServer {

    /**
     * 注册中心端口
     */
    private final int port;

    /**
     * 服务注册表
     */
    private final ServiceRegistry registry;

    public RegistryServer(int port) {
        this.port = port;
        this.registry = new ServiceRegistry();
    }

    /**
     * 启动注册中心服务器
     */
    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("[注册中心] 启动成功，监听端口: " + port);

                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> handleRequest(socket)).start();
                }
            } catch (IOException e) {
                System.err.println("[注册中心] 启动失败: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 处理客户端请求
     */
    private void handleRequest(Socket socket) {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            // 读取请求类型
            String requestType = (String) ois.readObject();

            switch (requestType) {
                case "REGISTER":
                    handleRegister(ois, oos);
                    break;
                case "DISCOVER":
                    handleDiscover(ois, oos);
                    break;
                case "DEREGISTER":
                    handleDeregister(ois, oos);
                    break;
                case "HEARTBEAT":
                    handleHeartbeat(ois, oos);
                    break;
                default:
                    System.err.println("[注册中心] 未知请求类型: " + requestType);
            }
        } catch (Exception e) {
            System.err.println("[注册中心] 处理请求异常: " + e.getMessage());
        }
    }

    /**
     * 处理服务注册
     */
    private void handleRegister(ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
        String serviceName = (String) ois.readObject();
        String host = (String) ois.readObject();
        int port = (int) ois.readObject();
        String instanceId = (String) ois.readObject();

        registry.register(serviceName, host, port, instanceId);
        oos.writeBoolean(true);
        oos.flush();
    }

    /**
     * 处理服务发现
     */
    private void handleDiscover(ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
        String serviceName = (String) ois.readObject();

        java.util.List<ServiceInstance> instances = registry.discover(serviceName);
        oos.writeObject(instances);
        oos.flush();
    }

    /**
     * 处理服务下线
     */
    private void handleDeregister(ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
        String serviceName = (String) ois.readObject();
        String instanceId = (String) ois.readObject();

        registry.deregister(serviceName, instanceId);
        oos.writeBoolean(true);
        oos.flush();
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
        String serviceName = (String) ois.readObject();
        String instanceId = (String) ois.readObject();

        registry.heartbeat(serviceName, instanceId);
        oos.writeBoolean(true);
        oos.flush();
    }

    public static void main(String[] args) {
        RegistryServer server = new RegistryServer(9000);
        server.start();
    }
}
