package com.github.eros.controller.user;

import com.github.eros.entry.UserInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 02:23
 */
public class UserInfoHolder {
    private static final Map<Integer, UserInfo> USER_INFO = new ConcurrentHashMap<>(256);

    public static void add(UserInfo userInfo) {
        USER_INFO.put(userInfo.getId(), userInfo);
    }

    public static void remove(Integer id) {
        USER_INFO.remove(id);
    }

    public static Map<Integer, UserInfo> getAll() {
        return USER_INFO;
    }

}
