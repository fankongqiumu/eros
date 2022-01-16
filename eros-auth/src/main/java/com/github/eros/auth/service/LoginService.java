package com.github.eros.auth.service;

import com.github.eros.auth.constant.AuthConstants;
import com.github.eros.auth.manager.UserManager;

import com.github.eros.auth.model.UserDTO;
import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.common.lang.MD5;
import com.github.eros.common.lang.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private UserManager userManager;

    public Result<String> login(UserDTO userDTO){
        if (null == userDTO || StringUtils.isBlank(userDTO.getPwd())){
            throw new ErosException(ErosError.BUSINIESS_ERROR, "username|mail and pwd can not be empty");
        }
        UserDTO queryUserDTO = userManager.queryUserDTO(userDTO);
        if (null == queryUserDTO){
            return Result.createFailWith(AuthConstants.ErrorInfo.USER_NOT_EXIST.getCode(),
                    AuthConstants.ErrorInfo.USER_NOT_EXIST.getMessage());
        }
        MD5 instance = MD5.getInstance();
        if (StringUtils.equals(instance.getMD5(userDTO.getPwd()), instance.getMD5(queryUserDTO.getPwd()))){
            // todo jwt 验证
        }
        return Result.createFailWith(AuthConstants.ErrorInfo.PWD_NOT_MATHING.getCode(),
                AuthConstants.ErrorInfo.PWD_NOT_MATHING.getMessage());

    }
}
