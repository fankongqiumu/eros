package com.github.eros.domain.user;

import com.github.eros.domain.BaseObject;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 14:14
 */
public class UserInfo extends BaseObject {
    private Long id;
    private String name;

    public UserInfo() {
    }

    public UserInfo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
