package com.github.eros.client;

import com.github.eros.client.forest.ForestFactory;
import com.github.eros.client.forest.service.FetchConfigService;
import com.github.eros.client.forest.service.WatchConfigService;
import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.common.lang.DefaultThreadFactory;
import com.github.eros.common.lang.FacadeLoader;
import com.github.eros.common.lang.Result;
import com.github.eros.common.model.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 00:17
 */
public abstract class ErosClientListener {

    private static final Logger logger = LoggerFactory.getLogger(ErosClientListener.class);

    private static volatile boolean listenersLoaded = false;

    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();

    private static volatile ScheduledExecutorService scheduledExecutorService;

    private static volatile ExecutorService fetchExecutorService;

    private static final Map<String, Object> configCache = new ConcurrentHashMap<>(256);

    private static final Map<String, ErosClientListener> CONFIG_LISTENER_HOLDER = new HashMap<>(32);

    protected static final Object lock = new Object();

    private final WatchConfigService configService;

    private final FetchConfigService fetchConfigService;

    private static final String localIp;

    static {
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new ErosException(ErosError.SYSTEM_ERROR, "InetAddress.getLocalHost().getHostAddress() error", e);
        }
    }

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
            this.configService = ForestFactory.createInstance(WatchConfigService.class);
            this.fetchConfigService = ForestFactory.createInstance(FetchConfigService.class);
        }
    }

    private void prepareOnReceiveConfigInfo(Object configInfo) {
        try {
            // todo 配置数据本地化备份 缓存
        } catch (Exception e) {
            logger.error("AbstractConfigListener.receiveConfigInfo failed,  configInfo={}", configInfo, e);
        }
    }

    public void fetchAndWatch() {
        fetchFromServer();
        watchAtFixedRate();
    }


    /**
     * 从服务端拉取配置
     */
    private void fetchFromServer() {
        checkOrInitFetchExecutorService();
        fetchExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Result<Config> result = fetchConfigService.fetch(getNamespace());
                if (result.isSuccess()) {
                    Config config = result.getData();
                    String data = config.getData();
                    prepareOnReceiveConfigInfo(data);
                    onReceiveConfigInfo(data);
                }
            }
        });
    }

    private void watchAtFixedRate() {
        checkOrInitScheduleExecutorService();
        // 长轮训监听
        final String clientKey = getApp() + ";" + getGrop() + ";" + localIp;
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                logger.info("{} watch [{}] on:{}", Thread.currentThread().getName(), getNamespace(), format.format(new Date()));
                Result<Void> configResult = configService.watch(getNamespace(), clientKey, HttpConstants.LONG_PULL_TIMEOUT_LONG_VALUE);
                if (configResult.isNotSuccess()) {
                    logger.error("{} watch [{}] config from server, respnse:{}, on{}", Thread.currentThread().getName(), getNamespace(), configResult, format.format(new Date()));
                    return;
                }
                if (HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode().equals(configResult.getMsgCode())) {
                    fetchFromServer();
                }
            }
        }, 3, 10, TimeUnit.SECONDS);
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

    private void checkOrInitScheduleExecutorService(){
        if (null == scheduledExecutorService || scheduledExecutorService.isShutdown()) {
            synchronized (lock) {
                if (null == scheduledExecutorService || scheduledExecutorService.isShutdown()) {
                    int coreSize = Math.min(getListeners().size(), availableProcessors);
                    scheduledExecutorService = new ScheduledThreadPoolExecutor(coreSize,
                            DefaultThreadFactory.defaultThreadFactory("pool-eros-watch-thread-")
                    );
                }
            }
        }
    }

    private void checkOrInitFetchExecutorService() {
        if (null == fetchExecutorService || fetchExecutorService.isShutdown()) {
            synchronized (lock) {
                if (null == fetchExecutorService || fetchExecutorService.isShutdown()) {
                    fetchExecutorService = new ThreadPoolExecutor(4, 10,
                            60L, TimeUnit.SECONDS,
                            new LinkedBlockingQueue(1024),
                            DefaultThreadFactory.defaultThreadFactory("pool-eros-fetch-thread-"),
                            new ThreadPoolExecutor.CallerRunsPolicy()
                    );
                }
            }
        }
    }

    static Collection<ErosClientListener> getListeners() {
        if (!listenersLoaded) {
            loadAndInstanceListeners();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                private volatile boolean hasShutdown = false;

                @Override
                public void run() {
                    synchronized (lock) {
                        if (!this.hasShutdown) {
                            this.hasShutdown = true;
                            long beginTime = System.currentTimeMillis();
                            nonListenerCallback();
                            long consumingTimeTotal = System.currentTimeMillis() - beginTime;
                            logger.info("Shutdown hook over, consuming total time(ms): {}", consumingTimeTotal);
                        }
                    }
                }
            }, "ShutdownHook"));
        }
        return CONFIG_LISTENER_HOLDER.values();
    }

    /**
     * 配置在eros.facade中的
     */
    private static void loadAndInstanceListeners() {
        if (listenersLoaded) {
            return;
        }
        synchronized (lock) {
            // 获取classpath下所有实现了本抽象类的类型 并实例化
            ClassLoader classLoaderToUse = ErosClientListener.class.getClassLoader();
            Set<String> listenerClassNames = FacadeLoader.loadListeners(ErosClientListener.class, classLoaderToUse);
            if (!listenerClassNames.isEmpty()) {
                for (String listenerClassName : listenerClassNames) {
                    try {
                        Class<?> listenerClass = Class.forName(listenerClassName);
                        FacadeLoader.instantiateFacade(listenerClassName, listenerClass, classLoaderToUse);
                    } catch (ClassNotFoundException e) {
                        logger.error("loadAndInstantiate [{}] failed [{}]", listenerClassName, e);
                    }
                }
            }
            listenersLoaded = true;
        }
    }

    static void nonListenerCallback() {
        long beginTime = System.currentTimeMillis();
        synchronized (lock) {
            if (null != scheduledExecutorService && !scheduledExecutorService.isShutdown()) {
                scheduledExecutorService.shutdown();
            }
            if (null != fetchExecutorService && !fetchExecutorService.isShutdown()) {
                fetchExecutorService.shutdown();
            }
        }
        logger.info("nonListenerCallback over, consuming total time(ms): {}", System.currentTimeMillis() - beginTime);
    }

}
