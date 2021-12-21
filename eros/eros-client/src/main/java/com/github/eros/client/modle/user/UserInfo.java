package com.github.eros.client.modle.user;

import com.github.eros.common.model.BaseObject;

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
