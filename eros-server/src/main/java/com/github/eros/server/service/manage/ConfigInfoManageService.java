package com.github.eros.server.service.manage;

import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.common.lang.NonNull;
import com.github.eros.common.model.Config;
import com.github.eros.server.cache.ConfigLocalCache;
import com.github.eros.server.cache.ErosServerLocalCache;
import com.github.eros.server.cache.LocalCacheKey;
import com.github.eros.server.common.ServerConstants;
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
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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

    @Autowired
    @Qualifier(ServerConstants.ExecutorConstants.MODIFIED_SYNC_DISPATCHER_SERVICE)
    private ExecutorService modifiedSyncDispatcherService;

    @Autowired
    @Qualifier(ServerConstants.ExecutorConstants.MODIFIED_SYNC_SCHEDULED_SERVICE)
    private ScheduledExecutorService modifiedSyncScheduledService;

    private SyncConfigModifiedService syncConfigModifiedService;

    @Override
    public void afterPropertiesSet() throws Exception {
        syncConfigModifiedService = ForestFactory.createInstance(SyncConfigModifiedService.class);
        if (null == syncConfigModifiedService){
            throw new ErosException(ErosError.SYSTEM_ERROR, "ForestFactory.createInstance(SyncConfigModifiedService.class) failed....");
        }
    }

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
        ((ConfigInfoManageService) AopContext.currentProxy()).configModifiedSync(namespace);
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
        ((ConfigInfoManageService) AopContext.currentProxy()).configModified(namespace);
    }

    /**
     * 发送配置发布事件
     * @param namespace
     */
    @Async(ServerConstants.ExecutorConstants.ASYNC_EVENT_TASK_EXECUTOR)
    public void configModified(String namespace) {
        eventPublisher.publishEvent(new ConfigModifyEvent(namespace));
    }

    /**
     * 发送配置同步事件
     * @param namespace
     */
    @Async(ServerConstants.ExecutorConstants.ASYNC_EVENT_TASK_EXECUTOR)
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
    @EventListener(classes = {ConfigModifySyncEvent.class})
    @Order(1)
    public void configModifiedSyncListener(ConfigModifySyncEvent configModifySyncEvent){
        modifiedSyncDispatcherService.execute(new ConfigModifyDispatcher(configModifySyncEvent));
    }


    class ConfigModifyDispatcher implements Runnable {
        private final ConfigModifySyncEvent configModifySyncEvent;

        ConfigModifyDispatcher(ConfigModifySyncEvent configModifySyncEvent){
            this.configModifySyncEvent = configModifySyncEvent;
        }

        @Override
        public void run() {
            String namespace = configModifySyncEvent.getNamespace();
            List<String> serverList = getServerList();
            if (serverList.isEmpty()){
                return;
            }
            String localIp = erosServerLocalCache.getString(LocalCacheKey.SERVER_NODE_IP);
            Integer port = erosServerLocalCache.getInteger(LocalCacheKey.SERVER_NODE_PORT);
            String selfServerDomain = localIp + ":" + port;
            serverList.remove(selfServerDomain);
            if (serverList.isEmpty()){
                return;
            }
            for (String serverDomain : serverList) {
                modifiedSyncScheduledService.submit(new ConfigModifyNotifyTask(serverDomain, namespace));
            }
        }
    }


    class ConfigModifyNotifyTask implements Runnable {

        private int retry = 10;
        private final String serverDomain;
        private final String namespace;
        private final long delay  = 5;

        ConfigModifyNotifyTask(String serverDomain, String namespace){
            this.serverDomain = serverDomain;
            this.namespace = namespace;
        }

        ConfigModifyNotifyTask(int retry, String serverDomain, String namespace){
            this.retry = retry;
            this.serverDomain = serverDomain;
            this.namespace = namespace;
        }

        @Override
        public void run() {
            List<String> serverList = getServerList();
            if (serverList.isEmpty()
                    || !serverList.contains(serverDomain)
                    || retry-- <= 0){
                return;
            }

            try {
                syncConfigModifiedService.sync(serverDomain, namespace, new OnSuccess() {
                    @Override
                    public void onSuccess(Object data, ForestRequest req, ForestResponse res) {
                        logger.info("syncConfigModifiedService.sync serverDomain [{}] success...", serverDomain);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ForestRuntimeException ex, ForestRequest req, ForestResponse res) {
                        logger.error("syncConfigModifiedService.sync serverDomain [{}] failed, req:[{}], res:[{}], error:[{}]",
                                serverDomain, req, res, ex);
                        // todo 报警
                        // 失败后在给定延迟后继续通知
                        ConfigModifyNotifyTask configModifyNotifyTask
                                = new ConfigModifyNotifyTask(retry, serverDomain, namespace);
                        modifiedSyncScheduledService.schedule(configModifyNotifyTask, delay, TimeUnit.SECONDS);
                    }
                });
            } catch (Exception exception){
                logger.error("syncConfigModifiedService.sync serverDomain [{}] failed, namespace:[{}], error:[{}]",
                        serverDomain, namespace, exception);
                // 失败后在给定延迟后继续通知
                ConfigModifyNotifyTask configModifyNotifyTask
                        = new ConfigModifyNotifyTask(retry, serverDomain, namespace);
                modifiedSyncScheduledService.schedule(configModifyNotifyTask, delay, TimeUnit.SECONDS);
                // todo 报警
            }
        }

    }


    /**
     * 获取app服务节点
     * @return
     */
    public List<String> getServerList(){
        List<String> appNodeList = null;
        try {
            appNodeList = nameServerClient.getAppNodeList(ErosAppConstants.DEFAULT_APP_NAME);
            // 此处缓存是为了在极端情况下依然可以获取到节点列表
            erosServerLocalCache.putObject(LocalCacheKey.SERVER_NODE_LIST, appNodeList);
        } catch (Exception exception){
            logger.error("nameServerClient.getAppNodeList failed, erros:", exception);
        }
        if (CollectionUtils.isEmpty(appNodeList)){
            appNodeList = (List<String>)erosServerLocalCache.getObject(LocalCacheKey.SERVER_NODE_LIST);
        }
        return appNodeList;
    }
}
