package com.github.eros.dal.mapper;

import com.github.eros.dal.model.User;


public interface UserMapper {
    Long insert(User user);

    User selectByUserId(Long  userId);

    User selectByUserName(String  userName);

    User selectByMail(String  mail);
}
