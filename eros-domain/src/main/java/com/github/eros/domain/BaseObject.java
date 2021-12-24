package com.github.eros.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class BaseObject implements Serializable {
    private static final long serialVersionUID = -2511772902219031389L;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
