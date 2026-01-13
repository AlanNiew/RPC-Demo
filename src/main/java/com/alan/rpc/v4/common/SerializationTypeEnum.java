package com.alan.rpc.v4.common;

/**
 * 序列化类型枚举
 * 定义支持的序列化方式
 */
public enum SerializationTypeEnum {
    /**
     * Java 原生序列化
     */
    JAVA("java", "Java 原生序列化"),

    /**
     * JSON 序列化
     */
    JSON("json", "JSON 序列化"),

    /**
     * Hessian 二进制序列化
     */
    HESSIAN("hessian", "Hessian 二进制序列化"),

    /**
     * Kryo 高性能序列化
     */
    KRYO("kryo", "Kryo 高性能序列化");

    private final String code;
    private final String description;

    SerializationTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取枚举类型
     */
    public static SerializationTypeEnum fromCode(String code) {
        for (SerializationTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的序列化类型: " + code);
    }
}
