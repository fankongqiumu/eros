package com.github.eros.server.service.manage;

import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.github.eros.common.model.Config;
import com.github.eros.server.cache.ConfigLocalCache;
import com.github.eros.server.cache.ErosServerLocalCache;
import com.github.eros.server.cache.LocalCacheKey;
import com.github.eros.server.constant.ErosAppConstants;
import com.github.eros.server.event.ConfigModifyEvent;
import com.github.eros.server.event.ConfigModifySyncEvent;
import com.github.eros.server.event.ConfigModifySyncEventPool;
import com.github.eros.server.event.ModifySyncEventObj;
import com.github.eros.server.forest.ForestFactory;
import com.github.eros.server.forest.service.SyncConfigModifiedService;
import com.github.eros.server.service.ConfigInfoService;
import com.github.nameserver.NameServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;


/**
 * @author fankongqiumu
 * @description 配置管理
 * @date 2021/12/21 17:58
 */
@Service
public class ConfigInfoManageService implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigInfoService configInfoService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigLocalCache configLocalCache;

    @Autowired
    private ConfigModifySyncEventPool configModifySyncEventPool;

    @Autowired
    private NameServerClient nameServerClient;

    @Autowired
    private ErosServerLocalCache erosServerLocalCache;

    private SyncConfigModifiedService syncConfigModifiedService;

    /**
     * 配置发布
     * @param config
     */
    public void publish(@NonNull Config config){
        final String namespace = config.getNamespace();
        // 1. 存本地 todo
        // 2.  receivePublish
        receivePublish(namespace);
        // 3. 发布配置同步事件
        configModifiedSync(namespace);
    }

    /**
     * 接收到配置发布的消息
     * @param namespace
     */
    public void receivePublish(@NonNull String namespace) {
        // 1. 更新本地缓存
        configLocalCache.refresh(namespace);
        // 2. 存事件缓存池
        ConfigModifySyncEvent configModifySyncEvent = buildConfigModifySyncEvent(namespace);
        configModifySyncEventPool.modifySyncEventCache(configModifySyncEvent);
        // 3. 发布配置变更事件
        configModified(namespace);
    }

    /**
     * 发送配置发布事件
     * @param namespace
     */
    @Async("asyncEventTaskExecutor")
    public void configModified(String namespace) {
        eventPublisher.publishEvent(new ConfigModifyEvent(namespace));
    }

    /**
     * 发送配置同步事件
     * @param namespace
     */
    @Async("asyncEventTaskExecutor")
    public void configModifiedSync(String namespace) {
        ConfigModifySyncEvent configModifySyncEvent = configModifySyncEventPool.getModifySyncEvent(namespace);
        eventPublisher.publishEvent(configModifySyncEvent);
    }

    private ConfigModifySyncEvent buildConfigModifySyncEvent(String namespace){
        Config config = configLocalCache.get(namespace);
        return new ConfigModifySyncEvent(
                new ModifySyncEventObj(namespace, config.getLastModified())
        );
    }

    /**
     * 配置同步事件处理
     * @param configModifySyncEvent
     */
    @Async("asyncEventTaskExecutor")
    @EventListener
    @Order(1)
    public void configModifiedSyncListener(ConfigModifySyncEvent configModifySyncEvent){
        String namespace = configModifySyncEvent.getNamespace();
        Set<String> serverList = getServerList();
        if (serverList.isEmpty()){
            return;
        }
        String localIp = (String)erosServerLocalCache.getObject(LocalCacheKey.SERVER_NODE_IP);
        Integer port = (Integer)erosServerLocalCache.getObject(LocalCacheKey.SERVER_NODE_PORT);
        final String selfServerDomain = localIp + ":" + port;
        serverList.remove(selfServerDomain);
        if (serverList.isEmpty()){
            return;
        }
        for (String serverDomain : serverList) {
            try {
                syncConfigModifiedService.sync(serverDomain, namespace, new OnSuccess() {
                    @Override
                    public void onSuccess(Object data, ForestRequest req, ForestResponse res) {
                        logger.info("syncConfigModifiedService.sync serverDomain [{}] success...", serverDomain);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ForestRuntimeException ex, ForestRequest req, ForestResponse res) {
                        logger.info("syncConfigModifiedService.sync serverDomain [{}] failed, req:[{}], res:[{}], error:[{}]",
                                serverDomain, req, res, ex);
                        // todo 通知
                    }
                });
            } catch (Exception exception){
                // todo 通知
            }
        }
    }

    /**
     * 获取app服务节点
     * todo 考虑可以做下缓存
     * @return
     */
    public Set<String> getServerList(){
        Set<String> appNodeList;
        try {
            appNodeList = nameServerClient.getAppNodeList(ErosAppConstants.DEFAULT_APP_NAME);
        } catch (Exception exception){
            logger.error("nameServerClient.getAppNodeList failed, erros:", exception);
            appNodeList = Collections.emptySet();
        }
        return appNodeList;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        syncConfigModifiedService = ForestFactory.createInstance(SyncConfigModifiedService.class);
    }
}
