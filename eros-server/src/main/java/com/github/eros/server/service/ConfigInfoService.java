package com.github.eros.server.service;

import com.github.eros.server.event.ConfigModifySyncEvent;
import com.github.eros.server.event.ConfigModifySyncEventPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fankongqiumu
 * @description 配置查询
 * @date 2021/12/21 17:57
 */
@Service
public class ConfigInfoService {

    @Autowired
    private ConfigModifySyncEventPool configModifySyncEventPool;

    public boolean hasEffectiveModifySyncEvent(String namespace, Long lastModified){
        ConfigModifySyncEvent modifySyncEvent = configModifySyncEventPool.getModifySyncEvent(namespace);
        if (null != modifySyncEvent){
            return modifySyncEvent.getLastModified() > lastModified;
        }
        return false;
    }
}
