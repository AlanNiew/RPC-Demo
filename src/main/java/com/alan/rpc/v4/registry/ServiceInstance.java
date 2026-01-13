package com.alan.rpc.v4.registry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务实例信息
 */
@Data
@AllArgsConstructor
public class ServiceInstance implements Serializable {

    /**
     * 服务名称（接口全限定名）
     */
    private String serviceName;

    /**
     * 服务主机地址
     */
    private String host;

    /**
     * 服务端口
     */
    private int port;

    /**
     * 实例ID（唯一标识）
     */
    private String instanceId;

    /**
     * 最后心跳时间戳
     */
    private long lastHeartbeat;

    public ServiceInstance(String serviceName, String host, int port, String instanceId) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.instanceId = instanceId;
        this.lastHeartbeat = System.currentTimeMillis();
    }

    /**
     * 更新心跳时间
     */
    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    /**
     * 获取服务地址
     */
    public String getAddress() {
        return host + ":" + port;
    }

    /**
     * 判断实例是否过期（超过30秒没有心跳）
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - lastHeartbeat > 30000;
    }
}
