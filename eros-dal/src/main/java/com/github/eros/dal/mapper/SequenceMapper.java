package com.github.eros.dal.mapper;

import org.apache.ibatis.annotations.Param;

public interface SequenceMapper {
    Long getCurrentVal(String sequenceName);

    void insert(String sequenceName);

    void updateByName(@Param("sequenceName") String sequenceName,
                      @Param("originValue") Long originValue,
                      @Param("newValue") Long newValue);
}
