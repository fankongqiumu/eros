package com.github.eros.server.event;

import com.github.eros.common.cache.LocalCache;
import com.github.eros.server.common.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * - 同步事件缓存池存在的意义是为了处理client的长轮训结束后到新轮训期间有新配置同步的情况
 * - 缓存的事件会在15s后过期
 */
@Component
public class ConfigModifySyncEventPool {

    @Resource(name = Constants.CacheConstants.CONFIG_MODIFY_SYNC_EVENT_CACHE)
    private LocalCache<String, ConfigModifySyncEvent> configModifySyncEventCache;

    public void modifySyncEventCache(ConfigModifySyncEvent modifySyncEvent){
        configModifySyncEventCache.putPuls(modifySyncEvent.getNamespace(), modifySyncEvent, modifySyncEvent.getLastModified());
    }

    public ConfigModifySyncEvent getModifySyncEvent(String namespace){
        return configModifySyncEventCache.get(namespace);
    }

}