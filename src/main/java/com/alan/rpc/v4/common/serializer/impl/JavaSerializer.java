package com.alan.rpc.v4.common.serializer.impl;

import com.alan.rpc.v4.common.SerializationTypeEnum;
import com.alan.rpc.v4.common.serializer.SerializationException;
import com.alan.rpc.v4.common.serializer.Serializer;

import java.io.*;

/**
 * Java 原生序列化实现
 * 使用 Java 的 ObjectOutputStream 和 ObjectInputStream
 * 要求对象实现 Serializable 接口
 */
public class JavaSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        if (obj == null) {
            throw new SerializationException("序列化对象不能为 null");
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Java 序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializationException {
        if (data == null || data.length == 0) {
            throw new SerializationException("反序列化数据不能为空");
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object obj = ois.readObject();
            if (!clazz.isInstance(obj)) {
                throw new SerializationException("类型不匹配，期望: " + clazz.getName() + "，实际: " + obj.getClass().getName());
            }
            return (T) obj;
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException("Java 反序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public SerializationTypeEnum getType() {
        return SerializationTypeEnum.JAVA;
    }
}
