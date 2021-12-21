package com.github.eros.entry;

import com.github.eros.common.model.BaseObject;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 02:20
 */
public class UserInfo extends BaseObject {
    private Integer id;
    private String name;

    public UserInfo(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
