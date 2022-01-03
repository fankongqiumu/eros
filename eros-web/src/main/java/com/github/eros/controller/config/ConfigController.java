package com.github.eros.controller.config;

import com.github.eros.common.lang.Result;
import com.github.eros.common.util.JsonUtils;
import com.github.eros.controller.user.UserInfoHolder;
import com.github.eros.server.model.UserInfo;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author fankongqiumu
 * @Description
 * @date 2021/12/17 11:41
 */
@RequestMapping("/config")
@RestController
public class ConfigController {

    @GetMapping("/get/{namespace}")
    public Result<String> findConfig(HttpServletRequest request, @PathVariable("namespace") String namespace){
        Map<Long, UserInfo> userInfoMap = UserInfoHolder.getAll();
        String configData = JsonUtils.toJsonString(userInfoMap.values());
        return Result.createSuccessWithData(configData);
    }

    @GetMapping("/list/{app}/{group}")
    public Result<String> findAppGroupConfigs(HttpServletRequest request,
                                           @PathVariable("app") String app,
                                           @PathVariable("group") String group){
        Map<Long, UserInfo> userInfoMap = UserInfoHolder.getAll();
        String configData = JsonUtils.toJsonString(userInfoMap.values());
        return Result.createSuccessWithData(configData);
    }

    @GetMapping("/list/{app}")
    public Result<String> findAppConfigs(HttpServletRequest request, @PathVariable("app") String app){
        Map<Long, UserInfo> userInfoMap = UserInfoHolder.getAll();
        String configData = JsonUtils.toJsonString(userInfoMap.values());
        return Result.createSuccessWithData(configData);
    }
}
