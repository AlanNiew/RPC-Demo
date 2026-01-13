package com.alan.rpc.v3.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC 请求对象
 * 封装客户端发起的 RPC 调用请求信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {

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
