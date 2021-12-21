package com.github.eros.listener;

import com.github.eros.client.listener.ErosClientListener;
import com.github.eros.client.modle.user.UserInfo;
import com.github.eros.common.util.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 02:11
 */
public class UserErosListener extends ErosClientListener {

    public static volatile Map<Long, UserInfo> userInfoConfig = new ConcurrentHashMap<>();

    @Override
    public String getAppName() {
        return "USER_APP";
    }

    @Override
    public String getDataId() {
        return "USER_DEFAULT";
    }

    @Override
    public String getGropId() {
        return "USER_DEFAULT_GROUP";
    }

    @Override
    protected void onReceiveConfigInfo(String configData) {
        if (null == configData || configData.isEmpty()) {
            logger.error("can not find userInfoConfig:{} in server", userInfoConfig);
            return;
        }
        try {
            List<UserInfo> userInfos = JsonUtils.parseList(configData, UserInfo.class);
            if (null != userInfos && !userInfos.isEmpty()) {
                userInfoConfig = userInfos.stream().collect(Collectors.toMap(UserInfo::getId, Function.identity()));
            }
        } catch (Exception e) {
            logger.error("parse configData:{}, error:{}", configData,  e);
        }
    }
}
