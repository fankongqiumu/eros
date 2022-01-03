package com.github.eros.dal.sequence;

public interface Sequence {
    Long getCurrentVal(String sequenceName);

    void insert(String sequenceName);

    void updateByName(String sequenceName, Long originValue, Long newValue);
}
