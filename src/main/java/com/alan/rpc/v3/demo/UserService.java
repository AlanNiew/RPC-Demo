package com.alan.rpc.v3.demo;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户ID获取用户名
     */
    String getUserName(Integer userId);

    /**
     * 创建用户
     */
    Boolean createUser(String username, Integer age);

    /**
     * 获取用户详细信息
     */
    String getUserInfo(Integer userId);
}
