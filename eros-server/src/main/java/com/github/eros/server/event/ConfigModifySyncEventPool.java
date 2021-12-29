package com.github.eros.server.event;

import com.github.eros.server.cache.LocalCache;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ConfigModifySyncEventPool {

    @Resource(name = "configModifySyncEventCache")
    private LocalCache<String, ConfigModifySyncEvent> configModifySyncEventCache;


    public void modifySyncEventCache(ConfigModifySyncEvent modifySyncEvent){
        configModifySyncEventCache.putPuls(modifySyncEvent.getNamespace(), modifySyncEvent, modifySyncEvent.getLastModified());
    }

    public ConfigModifySyncEvent getModifySyncEvent(String namespace){
        return configModifySyncEventCache.get(namespace);
    }

}
