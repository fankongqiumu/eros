package com.github.eros.server.service.manage;

import com.github.eros.domain.Config;
import com.github.eros.server.cache.ConfigLocalCache;
import com.github.eros.server.event.ConfigModifyEvent;
import com.github.eros.server.event.ConfigModifySyncEvent;
import com.github.eros.server.event.ConfigModifySyncEventPool;
import com.github.eros.server.event.ModifySyncEventObj;
import com.github.eros.server.service.ConfigInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;



/**
 * @author fankongqiumu
 * @description 配置管理
 * @date 2021/12/21 17:58
 */
@Service
public class ConfigInfoManageService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigInfoService configInfoService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigLocalCache configLocalCache;

    @Autowired
    private ConfigModifySyncEventPool configModifySyncEventPool;


    public void publish(@NonNull Config config){
        final String namespace = config.getNamespace();
        // 1. 存本地 todo
        // 2.  receivePublish
        receivePublish(namespace);
        // 3. 发布配置同步事件
        configModifiedSync(namespace);
    }

    public void receivePublish(@NonNull String namespace) {
        // 1. 更新本地缓存
        configLocalCache.refresh(namespace);
        // 2. 发布配置变更事件
        configModified(namespace);
    }

    @Async("asyncEventTaskExecutor")
    public void configModified(String namespace) {
        eventPublisher.publishEvent(new ConfigModifyEvent(namespace));
    }

    @Async("asyncEventTaskExecutor")
    public void configModifiedSync(String namespace) {
        Config config = configLocalCache.get(namespace);
        ConfigModifySyncEvent configModifySyncEvent = new ConfigModifySyncEvent(
                new ModifySyncEventObj(namespace, config.getLastModified())
        );
        configModifySyncEventPool.modifySyncEventCache(configModifySyncEvent);
        eventPublisher.publishEvent(configModifySyncEvent);
    }

    @Async("asyncEventTaskExecutor")
    @EventListener
    @Order(1)
    public void configModifiedSyncListener(ConfigModifySyncEvent configModifySyncEvent){
        // todo 配置变更同步逻辑
        String namespace = configModifySyncEvent.getNamespace();
    }

}
