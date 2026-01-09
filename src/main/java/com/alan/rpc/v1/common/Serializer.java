package com.alan.rpc.v1.common;

import java.io.*;

/**
 * 序列化工具类
 * 使用 Java 原生序列化实现对象与字节数组之间的转换
 */
public class Serializer {

    /**
     * 序列化：将对象转换为字节数组
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     * @throws IOException 序列化异常
     */
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    /**
     * 反序列化：将字节数组转换为对象
     *
     * @param data 序列化的字节数组
     * @return 反序列化后的对象
     * @throws IOException          反序列化异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }
}
