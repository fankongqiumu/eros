package com.github.eros.auth.manager;

import com.github.eros.auth.model.UserDTO;
import com.github.eros.dal.constant.SequenceContants;
import com.github.eros.dal.mapper.UserMapper;
import com.github.eros.dal.model.User;
import com.github.eros.dal.sequence.AbstractSequence;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class UserManager extends AbstractSequence {
    @Autowired
    private UserMapper userMapper;

    /**
     * 添加user
     * @param userDTO
     * @return
     */
    public Long userAdd(UserDTO userDTO){
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        if (null == user.getUserId()){
            user.setUserId(getSequence());
        }
        Date now = new Date();
        user.setGmtCreate(now);
        user.setGmtModified(now);
        userMapper.insert(user);
        return user.getUserId();
    }

    public UserDTO queryUserDTO(UserDTO userDTO){
        if (null == userDTO){
            return null;
        }
        UserDTO result = null; User user = null;
        if (null != userDTO.getUserId()) {
            user = userMapper.selectByUserId(userDTO.getUserId());
        } else if (null != userDTO.getUserName()){
            user = userMapper.selectByUserName(userDTO.getUserName());
        } else if (null != userDTO.getMail()){
            user = userMapper.selectByMail(userDTO.getMail());
        }
        if (null != user){
            result = new UserDTO();
            BeanUtils.copyProperties(user, result);
        }
        return result;
    }

    @Override
    protected String getName() {
        return SequenceContants.USER_SEQUENCE;
    }
}
