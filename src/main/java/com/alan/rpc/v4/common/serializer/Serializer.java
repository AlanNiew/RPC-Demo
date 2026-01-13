package com.alan.rpc.v4.common.serializer;

import com.alan.rpc.v4.common.SerializationTypeEnum;

/**
 * 序列化接口
 * 定义统一的序列化契约，所有序列化实现都需要实现此接口
 */
public interface Serializer {

    /**
     * 序列化：将对象转换为字节数组
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     * @throws SerializationException 序列化失败时抛出
     */
    byte[] serialize(Object obj) throws SerializationException;

    /**
     * 反序列化：将字节数组转换为指定类型的对象
     *
     * @param data  要反序列化的字节数组
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象
     * @throws SerializationException 反序列化失败时抛出
     */
    <T> T deserialize(byte[] data, Class<T> clazz) throws SerializationException;

    /**
     * 获取序列化类型
     *
     * @return 序列化类型枚举
     */
    SerializationTypeEnum getType();
}
