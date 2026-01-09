package com.alan.rpc.v2.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC 响应对象
 * 封装服务端返回的 RPC 调用结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse implements Serializable {

    /**
     * 请求唯一标识
     */
    private String requestId;

    /**
     * 调用返回结果
     */
    private Object result;

    /**
     * 异常信息
     */
    private Exception exception;

    /**
     * 创建成功响应
     */
    public static RpcResponse success(String requestId, Object result) {
        return new RpcResponse(requestId, result, null);
    }

    /**
     * 创建失败响应
     */
    public static RpcResponse fail(String requestId, Exception exception) {
        return new RpcResponse(requestId, null, exception);
    }
}
