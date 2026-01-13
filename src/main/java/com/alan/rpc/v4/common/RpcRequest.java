package com.alan.rpc.v4.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RPC 请求对象
 * 封装客户端发起的 RPC 调用请求信息
 * v4 版本：移除 Serializable 接口，支持多序列化方式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest {

    /**
     * 请求唯一标识
     */
    private String requestId;

    /**
     * 要调用的接口名称
     */
    private String interfaceName;

    /**
     * 要调用的方法名称
     */
    private String methodName;

    /**
     * 方法参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 方法参数值
     */
    private Object[] parameters;
}
