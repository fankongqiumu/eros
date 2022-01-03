package com.github.eros.controller.user;

import com.github.eros.server.model.UserInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 02:23
 */
public class UserInfoHolder {
    private static final Map<Long, UserInfo> USER_INFO = new ConcurrentHashMap<>(256);

    public static void add(UserInfo userInfo) {
        USER_INFO.put(userInfo.getId(), userInfo);
    }

    public static void remove(Long id) {
        USER_INFO.remove(id);
    }

    public static Map<Long, UserInfo> getAll() {
        return USER_INFO;
    }

}
