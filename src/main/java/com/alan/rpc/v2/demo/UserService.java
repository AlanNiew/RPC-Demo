package com.alan.rpc.v2.demo;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户ID获取用户名
     *
     * @param userId 用户ID
     * @return 用户名
     */
    String getUserName(Integer userId);

    /**
     * 创建用户
     *
     * @param username 用户名
     * @param age      年龄
     * @return 是否创建成功
     */
    Boolean createUser(String username, Integer age);

    /**
     * 获取用户详细信息
     *
     * @param userId 用户ID
     * @return 用户信息字符串
     */
    String getUserInfo(Integer userId);
}
