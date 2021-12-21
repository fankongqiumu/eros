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

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final Object lock = new Object();

    private static volatile ScheduledExecutorService scheduledExecutorService;

    private static final Map<String, Object>  configCache = new ConcurrentHashMap<>(256);

    private static final Map<String, ErosClientListener> CONFIG_LISTENER_HOLDER = new HashMap<>(32);

    private ConfigService configService;

    private ConfigInfo configInfo;

    public abstract String getAppName();

    public abstract String getDataId();

    public abstract String getGropId();

    protected abstract void onReceiveConfigInfo(String configData);

    protected ErosClientListener() {
        listenerBaseInfoValidate();
        synchronized (lock) {
            String dataId = getDataId();
            if (CONFIG_LISTENER_HOLDER.containsKey(dataId)) {
                throw new ErosException(ErosError.BUSINIESS_ERROR, "the dataId: " + dataId + " exist...");
            }
            CONFIG_LISTENER_HOLDER.put(dataId, this);
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
        scheduledExecutorService.scheduleWithFixedDelay(this, 0, 10, TimeUnit.SECONDS);
    }

    public static void nonListenerCallback() {
        if (null != scheduledExecutorService && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }

    private void listenerBaseInfoValidate() {
        if (StringUtils.isBlank(getAppName())) {
            throw new ErosException(ErosError.BUSINIESS_ERROR, "appName can not be empty...");
        }
        if (StringUtils.isBlank(getGropId())) {
            throw new ErosException(ErosError.BUSINIESS_ERROR, "groupId can not be empty...");
        }
        if (StringUtils.isBlank(getDataId())) {
            throw new ErosException(ErosError.BUSINIESS_ERROR, "dataId can not be empty...");
        }
    }

    @Override
    public void run() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.info("{} fetch config on:{}", Thread.currentThread().getName(), format.format(new Date()));
        Result<ConfigInfo> configResult = RetrofitClientUtil.executeCall(configService.fetch(getAppName(), getGropId(), getDataId()));
        if (configResult.isNotSuccess()) {
            logger.error("fetch [{}] config from server, respnse:{}", getDataId(), configResult);
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

    public static void loadAndInstanceListeners() {
        // 获取classpath下所有实现了本抽象类的类型 并实例化
        ErosFacadeLoader.loadListeners(ErosClientListener.class, ErosClientListener.class.getClassLoader());
    }

    public static Collection<ErosClientListener> getListeners(){
        if (null == scheduledExecutorService) {
            scheduledExecutorService = new ScheduledThreadPoolExecutor(5,
                    DefaultThreadFactory.defaultThreadFactory("pool-eros-fetch-thread-")
            );
        }
        if (CONFIG_LISTENER_HOLDER.isEmpty()) {
            loadAndInstanceListeners();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!scheduledExecutorService.isShutdown()) {
                scheduledExecutorService.shutdown();
            }
        }));
        return CONFIG_LISTENER_HOLDER.values();
    }
}
