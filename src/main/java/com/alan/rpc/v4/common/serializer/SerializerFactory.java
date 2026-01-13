package com.alan.rpc.v4.common.serializer;

import com.alan.rpc.v4.common.SerializationTypeEnum;
import com.alan.rpc.v4.common.serializer.impl.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器工厂
 * 负责创建和管理序列化器实例（单例模式）
 */
public class SerializerFactory {

    private static final Map<SerializationTypeEnum, Serializer> SERIALIZER_CACHE = new ConcurrentHashMap<>();

    static {
        // 预加载所有序列化器
        SERIALIZER_CACHE.put(SerializationTypeEnum.JAVA, new JavaSerializer());
        SERIALIZER_CACHE.put(SerializationTypeEnum.JSON, new JsonSerializer());
        SERIALIZER_CACHE.put(SerializationTypeEnum.HESSIAN, new HessianSerializer());
        SERIALIZER_CACHE.put(SerializationTypeEnum.KRYO, new KryoSerializer());
    }

    /**
     * 根据枚举类型获取序列化器实例（单例模式）
     *
     * @param type 序列化类型枚举
     * @return 序列化器实例
     */
    public static Serializer getSerializer(SerializationTypeEnum type) {
        Serializer serializer = SERIALIZER_CACHE.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("不支持的序列化类型: " + type);
        }
        return serializer;
    }

    /**
     * 根据 code 获取序列化器
     *
     * @param code 序列化类型代码
     * @return 序列化器实例
     */
    public static Serializer getSerializer(String code) {
        SerializationTypeEnum type = SerializationTypeEnum.fromCode(code);
        return getSerializer(type);
    }

    /**
     * 获取默认序列化器（Java 原生序列化）
     *
     * @return 默认序列化器实例
     */
    public static Serializer getDefaultSerializer() {
        return getSerializer(SerializationTypeEnum.JAVA);
    }
}
