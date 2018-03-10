package com.nowcoder.model;

import org.springframework.stereotype.Component;

/**
 * 用户实体
 */
@Component
public class HostHolder {
    /**
     *@Author LeonWang
     *@Description 这里是多线程
     */
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();
    }
}
