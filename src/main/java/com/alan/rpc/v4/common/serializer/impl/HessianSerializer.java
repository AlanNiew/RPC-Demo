package com.alan.rpc.v4.common.serializer.impl;

import com.alan.rpc.v4.common.SerializationTypeEnum;
import com.alan.rpc.v4.common.serializer.SerializationException;
import com.alan.rpc.v4.common.serializer.Serializer;
import com.caucho.hessian.io.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian 序列化实现
 * 使用 Hessian 二进制序列化协议，性能优于 Java 原生序列化
 */
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        if (obj == null) {
            throw new SerializationException("序列化对象不能为 null");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(bos);

        try {
            ho.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Hessian 序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializationException {
        if (data == null || data.length == 0) {
            throw new SerializationException("反序列化数据不能为空");
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        HessianInput hi = new HessianInput(bis);

        try {
            Object obj = hi.readObject();
            if (!clazz.isInstance(obj)) {
                throw new SerializationException("类型不匹配，期望: " + clazz.getName() + "，实际: " + obj.getClass().getName());
            }
            return (T) obj;
        } catch (IOException e) {
            throw new SerializationException("Hessian 反序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public SerializationTypeEnum getType() {
        return SerializationTypeEnum.HESSIAN;
    }
}
