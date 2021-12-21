package com.github.eros.common.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 14:11
 */
public class BaseObject implements Serializable {
    private static final long serialVersionUID = -2511772902219031389L;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
