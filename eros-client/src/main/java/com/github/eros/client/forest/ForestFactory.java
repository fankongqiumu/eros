package com.github.eros.client.forest;

import com.dtflys.forest.Forest;
import com.dtflys.forest.annotation.ForestClient;
import com.dtflys.forest.config.ForestConfiguration;
import com.github.eros.common.constant.HttpConstants;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ForestFactory {

//    private static final ForestConfiguration configuration = ForestConfiguration.configuration();

    private static final Map<Class<?>,Object> FOREST_SERVICE_CLIENT_HOLDER = new ConcurrentHashMap<>(32);

    private static final Object lock = new Object();

//    static {
//        // 连接池最大连接数，默认值为500
//        configuration.setMaxConnections(123);
//        // 每个路由的最大连接数，默认值为500
//        configuration.setMaxRouteConnections(222);
//        // 请求超时时间，单位为毫秒, 默认值为3000
//        configuration.setTimeout(HttpConstants.LONG_PULL_TIMEOUT);
//        configuration.setConnectTimeout(HttpConstants.LONG_PULL_TIMEOUT);
//        configuration.setReadTimeout(HttpConstants.LONG_PULL_TIMEOUT);
//        // 请求失败后重试次数，默认为0次不重试
//        configuration.setRetryCount(NumberUtils.INTEGER_ZERO);
//        configuration.setMaxRetryCount(NumberUtils.INTEGER_ZERO);
//        // 打开或关闭日志，默认为true
//        configuration.setLogEnabled(true);
//
//    }

    public static <Interface> Interface createInstance(Class<Interface> interfaceClass) {
        if (FOREST_SERVICE_CLIENT_HOLDER.containsKey(interfaceClass)) {
            return (Interface) FOREST_SERVICE_CLIENT_HOLDER.get(interfaceClass);
        }
        synchronized (lock) {
            if (!FOREST_SERVICE_CLIENT_HOLDER.containsKey(interfaceClass)) {
                Interface instance = Forest.client(interfaceClass);
                FOREST_SERVICE_CLIENT_HOLDER.put(interfaceClass, instance);
            }
        }
        return (Interface)FOREST_SERVICE_CLIENT_HOLDER.get(interfaceClass);
    }
}
