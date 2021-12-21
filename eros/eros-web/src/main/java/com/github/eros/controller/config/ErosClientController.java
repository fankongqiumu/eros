package com.github.eros.controller.config;


import com.github.eros.client.retrofit.entry.ConfigInfo;
import com.github.eros.common.lang.MD5;
import com.github.eros.common.model.Result;
import com.github.eros.common.util.JsonUtils;
import com.github.eros.controller.user.UserInfoHolder;
import com.github.eros.entry.UserInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/config/fetch")
    public Result<ConfigInfo> getConfig(HttpServletRequest request, @RequestParam("appName") String appName,
                            @RequestParam("groupId") String groupId,
                            @RequestParam("dataId") String dataId){
        Map<Integer, UserInfo> userInfoMap = UserInfoHolder.getAll();
        String configData = JsonUtils.toJsonString(userInfoMap.values());
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setConfigData(configData);
        configInfo.setLastModified(System.currentTimeMillis());
        configInfo.setMd5(MD5.getInstance().getMD5(configData));
        return Result.createSuccessWithData(configInfo);
    }


}
