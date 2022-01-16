package com.github.eros.dal.mapper;

import com.github.eros.dal.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface UserMapper {
    Long insert(@Param("items") List<User> user);

    User selectByUserId(Long  userId);

    User selectByUserName(String  userName);

    User selectByMail(String  mail);
}
