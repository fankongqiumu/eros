package com.github.eros.user.model;

/**
 * @author fankongqiumu
 * @description 用户基础模型
 * @date 2021/12/17 14:08
 */
public class User {
    private Long userId;
    private String userName;

    public User() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
