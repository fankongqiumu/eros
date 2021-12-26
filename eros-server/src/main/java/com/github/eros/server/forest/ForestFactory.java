package com.github.eros.server.forest;

import com.dtflys.forest.backend.httpclient.HttpclientBackend;
import com.dtflys.forest.config.ForestConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ForestFactory {

    private static final ForestConfiguration configuration = ForestConfiguration.configuration();

    private static final Map<Class<?>,Object> FOREST_SERVICE_CLIENT_HOLDER = new ConcurrentHashMap<>(32);

    private static final Object lock = new Object();

    static {
        configuration.setBackendName(HttpclientBackend.NAME);
        // 连接池最大连接数，默认值为500
        configuration.setMaxConnections(123);
        // 每个路由的最大连接数，默认值为500
        configuration.setMaxRouteConnections(222);
        // 请求超时时间，单位为毫秒, 默认值为3000
        configuration.setTimeout(3000);
        configuration.setConnectTimeout(3000);
        configuration.setReadTimeout(3000);
        // 请求失败后重试次数，默认为0次不重试
        configuration.setRetryCount(0);
        configuration.setMaxRetryCount(0);
        configuration.setLogEnabled(true);
    }

    public static <Interface> Interface createInstance(Class<Interface> interfaceClass) {
        if (FOREST_SERVICE_CLIENT_HOLDER.containsKey(interfaceClass)) {
            return (Interface) FOREST_SERVICE_CLIENT_HOLDER.get(interfaceClass);
        }
        synchronized (lock) {
            if (!FOREST_SERVICE_CLIENT_HOLDER.containsKey(interfaceClass)) {
                Interface instance = configuration.createInstance(interfaceClass);
                FOREST_SERVICE_CLIENT_HOLDER.put(interfaceClass, instance);
                return instance;
            }
        }
        return (Interface)FOREST_SERVICE_CLIENT_HOLDER.get(interfaceClass);
    }
}
