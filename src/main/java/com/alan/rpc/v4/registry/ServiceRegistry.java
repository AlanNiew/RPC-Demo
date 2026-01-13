package com.alan.rpc.v4.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册中心
 * 管理所有服务的注册信息，支持服务注册、发现、下线
 */
public class ServiceRegistry {

    /**
     * 服务注册表：接口名称 -> 服务实例列表
     */
    private final Map<String, List<ServiceInstance>> registry = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName    服务名称（接口全限定名）
     * @param host           服务主机地址
     * @param port           服务端口
     * @param instanceId     实例ID
     */
    public synchronized void register(String serviceName, String host, int port, String instanceId) {
        List<ServiceInstance> instances = registry.computeIfAbsent(serviceName, k -> new ArrayList<>());

        // 检查是否已存在该实例
        for (ServiceInstance instance : instances) {
            if (instance.getInstanceId().equals(instanceId)) {
                // 更新心跳时间
                instance.updateHeartbeat();
                System.out.println("[注册中心] 服务实例已存在，更新心跳: " + serviceName + " -> " + host + ":" + port);
                return;
            }
        }

        // 新增服务实例
        ServiceInstance instance = new ServiceInstance(serviceName, host, port, instanceId);
        instances.add(instance);
        System.out.println("[注册中心] 注册服务: " + serviceName + " -> " + host + ":" + port + " (实例: " + instanceId + ")");
    }

    /**
     * 发现服务
     *
     * @param serviceName 服务名称
     * @return 服务实例列表，如果不存在返回空列表
     */
    public synchronized List<ServiceInstance> discover(String serviceName) {
        List<ServiceInstance> instances = registry.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            System.out.println("[注册中心] 服务未找到: " + serviceName);
            return new ArrayList<>();
        }

        // 过滤掉过期的实例（超过30秒没有心跳）
        List<ServiceInstance> validInstances = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (ServiceInstance instance : instances) {
            if (now - instance.getLastHeartbeat() < 30000) {
                validInstances.add(instance);
            }
        }

        // 更新注册表，移除过期实例
        if (validInstances.size() != instances.size()) {
            registry.put(serviceName, validInstances);
            System.out.println("[注册中心] 清理过期实例: " + serviceName + ", 剩余 " + validInstances.size() + " 个实例");
        }

        System.out.println("[注册中心] 发现服务: " + serviceName + ", 可用实例: " + validInstances.size());
        return validInstances;
    }

    /**
     * 服务下线
     *
     * @param serviceName 服务名称
     * @param instanceId  实例ID
     */
    public synchronized void deregister(String serviceName, String instanceId) {
        List<ServiceInstance> instances = registry.get(serviceName);
        if (instances != null) {
            instances.removeIf(instance -> instance.getInstanceId().equals(instanceId));
            System.out.println("[注册中心] 服务下线: " + serviceName + " (实例: " + instanceId + ")");
        }
    }

    /**
     * 心跳检测
     *
     * @param serviceName 服务名称
     * @param instanceId  实例ID
     */
    public synchronized void heartbeat(String serviceName, String instanceId) {
        List<ServiceInstance> instances = registry.get(serviceName);
        if (instances != null) {
            for (ServiceInstance instance : instances) {
                if (instance.getInstanceId().equals(instanceId)) {
                    instance.updateHeartbeat();
                    break;
                }
            }
        }
    }

    /**
     * 获取所有注册的服务
     */
    public synchronized Map<String, List<ServiceInstance>> getAllServices() {
        return new ConcurrentHashMap<>(registry);
    }
}
