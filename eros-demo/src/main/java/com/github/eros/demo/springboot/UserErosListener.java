package com.github.eros.demo.springboot;

import com.github.eros.client.ErosClientListener;
import com.github.eros.common.util.JsonUtils;
import com.github.eros.demo.model.UserInfo;
import com.github.eros.starter.annotation.ErosListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 02:11
 */
@ErosListener
public class UserErosListener extends ErosClientListener {
    private  final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static volatile Map<Long, UserInfo> userInfoConfig = new ConcurrentHashMap<>();

    @Override
    public String getApp() {
        return "USER_APP";
    }

    @Override
    public String getNamespace() {
        return "USER_DEFAULT";
    }

    @Override
    public String getGrop() {
        return "USER_DEFAULT_GROUP";
    }

    @Override
    protected void onReceiveConfigInfo(String configData) {
        if (null == configData || configData.isEmpty() || JsonUtils.isBlank(configData)) {
            logger.error("server configData is blank, userInfoConfig:{}", userInfoConfig);
            return;
        }
        try {
            List<UserInfo> userInfos = JsonUtils.parseList(configData, UserInfo.class);
            if (null != userInfos && !userInfos.isEmpty()) {
                Map<Long, UserInfo> tem = new ConcurrentHashMap<>(userInfos.size());
                for (UserInfo userInfo : userInfos) {
                    tem.put(userInfo.getId(), userInfo);
                }
                userInfoConfig = tem;
            }
        } catch (Exception e) {
            logger.error("parse configData:{}, error:{}", configData,  e);
        }
    }
}
