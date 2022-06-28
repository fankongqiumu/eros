package com.github.eros.client.forest;

import com.dtflys.forest.backend.httpclient.HttpclientBackend;
import com.dtflys.forest.callback.AddressSource;
import com.dtflys.forest.config.ForestConfiguration;
import com.dtflys.forest.http.*;
import com.github.eros.common.cache.LocalCache;
import com.github.eros.common.constant.Constants;
import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.nameserver.NameServerClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class ForestFactory {

    private static final ForestConfiguration configuration = ForestConfiguration.configuration();

    private static final Map<Class<?>,Object> FOREST_SERVICE_CLIENT_HOLDER = new ConcurrentHashMap<>(32);

    private static final AtomicBoolean inited = new AtomicBoolean(false);

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
        configuration.setRetryCount(Constants.INTEGER_ZERO);
        configuration.setMaxRetryCount(Constants.INTEGER_ZERO);
//        Map<ForestDataType, ForestConverter> forestDataTypeForestConverterMap = configuration.getConverterMap();
//        ForestGsonConverter gsonConverter = new ForestGsonConverter();
//        forestDataTypeForestConverterMap.put(ForestDataType.JSON, gsonConverter);
//        configuration.setConverterMap(forestDataTypeForestConverterMap);
        // 更换JSON转换器Gson
//        configuration.setJsonConverter(new ForestGsonConverter());
        // 打开或关闭日志，默认为true
        configuration.setLogEnabled(true);
    }


    public static void initClientAddress(List<Address> addressList){
        if (inited.compareAndSet(false, true)){
            ClientAddressSource.initNameServer(addressList);
        }
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

    public static class ClientAddressSource implements AddressSource {

        private LocalCache<String, List<ForestAddress>> forestAddressCache = LocalCache.buildSimpleSynCache(256);;

        private static volatile NameServerClient nameServerClient;

        private static void initNameServer(List<Address> addressList){
            if (null == nameServerClient){
                if (null == addressList || addressList.isEmpty()){
                    throw new ErosException(ErosError.SYSTEM_ERROR, "nameserver domain list is empty...");
                }
                List<String> nameServerDomains = new ArrayList<>(addressList.size());
                for (Address address : addressList) {
                    nameServerDomains.add(address.getHost() + ":" + address.getPort());
                }
                nameServerClient = NameServerClient.getInstance(nameServerDomains, true);
            }
        }


        @Override
        public ForestAddress getAddress(ForestRequest forestRequest) {
            List<ForestAddress> addressList = forestAddressCache.get(Constants.ErosConstants.DEFAULT_APP_NAME);
            if (addressList.isEmpty()){
                loadAddress();
                addressList = forestAddressCache.get(Constants.ErosConstants.DEFAULT_APP_NAME);
            }
            if (addressList.isEmpty()){
                // TODO 待建重试任务 和本地缓存读取
                throw new ErosException(ErosError.SYSTEM_ERROR, "no active server...");
            }
            // 上次连接的server明确指明去连接其他serverNode
            ForestHeader preDomainHeader = forestRequest.getHeader("preDomain");
            if (null != preDomainHeader){
                String preDomain = preDomainHeader.getValue();
                addressList = addressList.stream()
                        .filter(forestAddress -> !forestAddress.getHost().equals(preDomain))
                        .collect(Collectors.toList());
                ForestHeader preDomainPortHeader = forestRequest.getHeader("preDomainPort");
                ForestHeaderMap headers = forestRequest.headers();
                headers.remove("preDomain");
                headers.remove("preDomainPort");
                if (addressList.isEmpty()){
                    String port = preDomainPortHeader.getValue();
                    return new ForestAddress(preDomain, Integer.getInteger(port));
                }
            }
            // 打乱顺序随机获取
            // todo 此处指定随机数获取即可
            Collections.shuffle(addressList);
            return addressList.get(Constants.INTEGER_ZERO);
        }

        // todo 动态从nameserver获取
        public void  loadAddress(){
            List<String> appNodeList = nameServerClient.getAppNodeList(Constants.ErosConstants.DEFAULT_APP_NAME);
            if (!appNodeList.isEmpty()){
                List<ForestAddress> addressList = new ArrayList<>(appNodeList.size());
                for (String appNode : appNodeList) {
                    String[] split = appNode.split(":");
                    addressList.add(new ForestAddress(split[0], Integer.getInteger(split[1])));
                }
                forestAddressCache.put(Constants.ErosConstants.DEFAULT_APP_NAME, addressList);
            }
//            WATCH_ADDRESS_HOLDER.add(new ForestAddress("eros.test.b2c.srv", 80));
        }

        /**
         * client 保活 ，server检测
         */
        public void addressChange(){

        }

    }
}
