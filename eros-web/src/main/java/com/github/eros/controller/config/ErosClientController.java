package com.github.eros.controller.config;


import com.github.eros.common.lang.MD5;
import com.github.eros.common.model.Result;
import com.github.eros.common.util.JsonUtils;
import com.github.eros.controller.user.UserInfoHolder;
import com.github.eros.entry.UserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author fankongqiumu
 * @Description
 * @date 2021/12/17 11:41
 */
@RestController
public class ErosClientController {

    @GetMapping("/fetch/{namespace}")
    public Result<String> fetch(HttpServletRequest request, @PathVariable("namespace") String namespace){
        Map<Integer, UserInfo> userInfoMap = UserInfoHolder.getAll();
        String configData = JsonUtils.toJsonString(userInfoMap.values());
        return Result.createSuccessWithData(configData);
    }


}
