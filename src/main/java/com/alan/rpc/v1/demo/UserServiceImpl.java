package com.alan.rpc.v1.demo;

/**
 * 用户服务实现类
 */
public class UserServiceImpl implements UserService {

    @Override
    public String getUserName(Integer userId) {
        System.out.println("[服务端] 执行 getUserName，参数: userId=" + userId);
        // 模拟数据库查询
        return "用户-" + userId;
    }

    @Override
    public Boolean createUser(String username, Integer age) {
        System.out.println("[服务端] 执行 createUser，参数: username=" + username + ", age=" + age);
        // 模拟创建用户
        return true;
    }
}
