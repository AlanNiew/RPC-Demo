package com.alan.rpc.v4.common.serializer.impl;

import com.alan.rpc.v4.common.SerializationTypeEnum;
import com.alan.rpc.v4.common.serializer.SerializationException;
import com.alan.rpc.v4.common.serializer.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON 序列化实现
 * 使用 Jackson 库进行 JSON 序列化和反序列化
 */
public class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        if (obj == null) {
            throw new SerializationException("序列化对象不能为 null");
        }

        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new SerializationException("JSON 序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializationException {
        if (data == null || data.length == 0) {
            throw new SerializationException("反序列化数据不能为空");
        }

        try {
            return objectMapper.readValue(data, clazz);
        } catch (Exception e) {
            throw new SerializationException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public SerializationTypeEnum getType() {
        return SerializationTypeEnum.JSON;
    }
}
