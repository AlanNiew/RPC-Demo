package com.alan.rpc.v4.common.serializer;

/**
 * 序列化异常
 * 统一封装所有序列化相关的异常
 */
public class SerializationException extends RuntimeException {

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
