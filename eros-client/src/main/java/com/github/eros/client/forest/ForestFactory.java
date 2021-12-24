package com.github.eros.client.forest;

import com.dtflys.forest.backend.httpclient.HttpclientBackend;
import com.dtflys.forest.config.ForestConfiguration;
import com.dtflys.forest.converter.ForestConverter;
import com.dtflys.forest.converter.json.ForestGsonConverter;
import com.dtflys.forest.utils.ForestDataType;
import com.dtflys.forest.utils.RequestNameValue;
import com.github.eros.common.constant.Contants;
import com.github.eros.common.constant.HttpConstants;

import java.util.*;
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
        configuration.setTimeout(HttpConstants.LONG_PULL_TIMEOUT_INT_VALUE);
        configuration.setConnectTimeout(HttpConstants.CONNECT_TIMEOUT);
        configuration.setReadTimeout(HttpConstants.LONG_PULL_TIMEOUT_INT_VALUE);
        // 请求失败后重试次数，默认为0次不重试
        configuration.setRetryCount(Contants.INTEGER_ZERO);
        configuration.setMaxRetryCount(Contants.INTEGER_ZERO);
//        Map<ForestDataType, ForestConverter> forestDataTypeForestConverterMap = configuration.getConverterMap();
//        ForestGsonConverter gsonConverter = new ForestGsonConverter();
//        forestDataTypeForestConverterMap.put(ForestDataType.JSON, gsonConverter);
//        configuration.setConverterMap(forestDataTypeForestConverterMap);
        // 更换JSON转换器Gson
//        configuration.setJsonConverter(new ForestGsonConverter());
        // 打开或关闭日志，默认为true
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
