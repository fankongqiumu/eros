package com.github.eros.server.service.manage;

import com.github.eros.server.service.ConfigInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fankongqiumu
 * @description 配置管理
 * @date 2021/12/21 17:58
 */
@Service
public class ConfigInfoManageService {

    private static final Map<String, String> namespaceConfigMap = new ConcurrentHashMap<>(256);

    @Autowired
    private ConfigInfoService configInfoService;

    public void publish(@NonNull String namespace, @NonNull String config){
        namespaceConfigMap.put(namespace, config);
    }



}
