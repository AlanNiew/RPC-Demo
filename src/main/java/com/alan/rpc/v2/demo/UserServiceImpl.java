package com.alan.rpc.v2.demo;

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

    @Override
    public String getUserInfo(Integer userId) {
        System.out.println("[服务端] 执行 getUserInfo，参数: userId=" + userId);
        // 模拟查询用户详细信息
        return "ID:" + userId + ",姓名:用户-" + userId + ",年龄:25";
    }
}
