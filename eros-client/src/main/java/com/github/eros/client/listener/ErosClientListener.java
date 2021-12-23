package com.github.eros.client.listener;

import com.github.eros.client.retrofit.entry.ConfigInfo;
import com.github.eros.client.retrofit.service.ConfigService;
import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.common.lang.DefaultThreadFactory;
import com.github.eros.common.lang.ErosFacadeLoader;
import com.github.eros.common.model.Result;
import com.github.eros.common.retrofit.client.RetrofitClient;
import com.github.eros.common.retrofit.util.RetrofitClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 00:17
 */
public abstract class ErosClientListener implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(ErosClientListener.class);

    protected static final Object lock = new Object();

    private static volatile boolean listenersLoaded = false;

    private static volatile ScheduledExecutorService scheduledExecutorService;

    private static final Map<String, Object>  configCache = new ConcurrentHashMap<>(256);

    private static final Map<String, ErosClientListener> CONFIG_LISTENER_HOLDER = new HashMap<>(32);

    private ConfigService configService;

    private ConfigInfo configInfo;

    public abstract String getApp();

    public abstract String getNamespace();

    public abstract String getGrop();

    protected abstract void onReceiveConfigInfo(String configData);

    protected ErosClientListener() {
        listenerBaseInfoValidate();
        synchronized (lock) {
            String namespace = getNamespace();
            if (CONFIG_LISTENER_HOLDER.containsKey(namespace)) {
                throw new ErosException(ErosError.BUSINIESS_ERROR, "the namespace: " + namespace + " exist...");
            }
            CONFIG_LISTENER_HOLDER.put(namespace, this);
            RetrofitClient client = RetrofitClient.Builder.build(ConfigService.class);
            this.configService = client.getRetrofitServiceInstance(ConfigService.class);
        }
    }

    private void prepareOnReceiveConfigInfo(ConfigInfo configInfo) {
        try {
            // todo 配置数据本地化备份 缓存
        } catch (Exception e) {
            logger.error("AbstractConfigListener.receiveConfigInfo failed,  configInfo={}", configInfo, e);
        }
    }

    public void fetchAtFixedRate() {
        // todo 长连接拉取
        scheduledExecutorService.scheduleWithFixedDelay(this, 1, 10, TimeUnit.SECONDS);
    }

    public static void nonListenerCallback() {
        if (null != scheduledExecutorService && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }

    private void listenerBaseInfoValidate() {
        if (StringUtils.isBlank(getApp())) {
            throw new ErosException(ErosError.BUSINIESS_ERROR, "app can not be empty...");
        }
        if (StringUtils.isBlank(getGrop())) {
            throw new ErosException(ErosError.BUSINIESS_ERROR, "group can not be empty...");
        }
        if (StringUtils.isBlank(getNamespace())) {
            throw new ErosException(ErosError.BUSINIESS_ERROR, "namespace can not be empty...");
        }
    }

    @Override
    public void run() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.info("{} fetch config on:{}", Thread.currentThread().getName(), format.format(new Date()));
        Result<ConfigInfo> configResult = RetrofitClientUtil.executeCall(configService.watch( getNamespace(), getApp(), getGrop()));
        if (configResult.isNotSuccess()) {
            logger.error("fetch [{}] config from server, respnse:{}, on{}", getNamespace(), configResult, format.format(new Date()));
            return;
        }
        ConfigInfo configResultData = configResult.getData();
        boolean hasUpdate = hasUpdate(this.configInfo, configResultData);
        if (hasUpdate) {
            this.configInfo = configResultData;
            this.prepareOnReceiveConfigInfo(configResultData);
            this.onReceiveConfigInfo(configResultData.getConfigData());
        }
    }

    private boolean hasUpdate(ConfigInfo oldConfigInfo, ConfigInfo fetchConfigInfo){
        boolean oldConfigInfoIsNull = (null == oldConfigInfo);
        boolean fetchConfigInfoIsNonNull = (null != fetchConfigInfo);
        if (oldConfigInfoIsNull && fetchConfigInfoIsNonNull) {
            return true;
        }
        if (fetchConfigInfoIsNonNull) {
            return !oldConfigInfo.getMd5().equals(fetchConfigInfo.getConfigData());
        }
        return false;
    }

    private static void loadAndInstanceListeners() {
        if (listenersLoaded) {
            return;
        }
        synchronized (lock) {
            // 获取classpath下所有实现了本抽象类的类型 并实例化
            ErosFacadeLoader.loadListeners(ErosClientListener.class, ErosClientListener.class.getClassLoader());
            listenersLoaded = true;
        }
    }

    public static Collection<ErosClientListener> getListeners(){
        if (null == scheduledExecutorService) {
            scheduledExecutorService = new ScheduledThreadPoolExecutor(5,
                    DefaultThreadFactory.defaultThreadFactory("pool-eros-fetch-thread-")
            );
        }
        if (CONFIG_LISTENER_HOLDER.isEmpty()) {
            synchronized (lock) {
                if (CONFIG_LISTENER_HOLDER.isEmpty()) {
                    loadAndInstanceListeners();
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            private volatile boolean hasShutdown = false;
            @Override
            public void run() {
                synchronized (lock) {
                    if (!this.hasShutdown) {
                        this.hasShutdown = true;
                        long beginTime = System.currentTimeMillis();
                        scheduledExecutorService.shutdown();
                        long consumingTimeTotal = System.currentTimeMillis() - beginTime;
                        logger.info("Shutdown hook over, consuming total time(ms): {}", consumingTimeTotal);
                    }
                }
            }
        }, "ShutdownHook"));
        return CONFIG_LISTENER_HOLDER.values();
    }
}
