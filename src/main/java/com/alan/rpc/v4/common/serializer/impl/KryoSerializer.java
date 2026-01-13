package com.alan.rpc.v4.common.serializer.impl;

import com.alan.rpc.v4.common.SerializationTypeEnum;
import com.alan.rpc.v4.common.serializer.SerializationException;
import com.alan.rpc.v4.common.serializer.Serializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo 序列化实现
 * 使用 Kryo 高性能序列化库，性能优于 Hessian 和 Java 原生序列化
 * 注意：Kryo 不是线程安全的，使用 ThreadLocal 保证线程安全
 */
public class KryoSerializer implements Serializer {

    /**
     * Kryo 不是线程安全的，使用 ThreadLocal 为每个线程创建独立的 Kryo 实例
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 允许未注册的类序列化（生产环境建议注册所有类以提高性能）
        kryo.setRegistrationRequired(false);
        // 支持循环引用
        kryo.setReferences(true);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        if (obj == null) {
            throw new SerializationException("序列化对象不能为 null");
        }

        Kryo kryo = KRYO_THREAD_LOCAL.get();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output output = new Output(bos)) {
            kryo.writeClassAndObject(output, obj);
            output.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Kryo 序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializationException {
        if (data == null || data.length == 0) {
            throw new SerializationException("反序列化数据不能为空");
        }

        Kryo kryo = KRYO_THREAD_LOCAL.get();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             Input input = new Input(bis)) {
            Object obj = kryo.readClassAndObject(input);
            if (!clazz.isInstance(obj)) {
                throw new SerializationException("类型不匹配，期望: " + clazz.getName() + "，实际: " + obj.getClass().getName());
            }
            return (T) obj;
        } catch (Exception e) {
            throw new SerializationException("Kryo 反序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public SerializationTypeEnum getType() {
        return SerializationTypeEnum.KRYO;
    }
}
