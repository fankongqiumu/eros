package com.github.eros.common.retrofit.service;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.retrofit.exception.ErosRetrofitException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/19 02:58
 */
public class RetrofitServiceProxy {

    private static final Map<Class<?>, Object> RETROFIT_SERVICE_PROXY_HOLDER = new ConcurrentHashMap<>(256);

    public static <InterfaceService> InterfaceService getInstance (Class<InterfaceService> interfaceServiceClass) {
        Objects.requireNonNull(interfaceServiceClass);
        if (!interfaceServiceClass.isInterface()) {
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "the interfaceServiceClass must be interface...");
        }
        if (!RETROFIT_SERVICE_PROXY_HOLDER.containsKey(interfaceServiceClass)) {
            synchronized (RetrofitServiceProxy.class){
                if (!RETROFIT_SERVICE_PROXY_HOLDER.containsKey(interfaceServiceClass)) {
                    RETROFIT_SERVICE_PROXY_HOLDER.putIfAbsent(interfaceServiceClass, newProxyInstance(interfaceServiceClass));
                }
            }
        }
        return (InterfaceService) RETROFIT_SERVICE_PROXY_HOLDER.get(interfaceServiceClass);
    }

    private static synchronized <InterfaceService> InterfaceService newProxyInstance(Class<InterfaceService> interfaceServiceClass){
        Objects.requireNonNull(interfaceServiceClass);
        InterfaceService interfaceService = (InterfaceService)RETROFIT_SERVICE_PROXY_HOLDER.get(interfaceServiceClass);
        if (null == interfaceService) {
            interfaceService = (InterfaceService)Proxy.newProxyInstance(interfaceServiceClass.getClassLoader(),
                    new Class[]{interfaceServiceClass},
                    new RetrofitServiceInvocationHandler());
            RETROFIT_SERVICE_PROXY_HOLDER.putIfAbsent(interfaceServiceClass, interfaceService);
        }
        return interfaceService;
    }

    private static class RetrofitServiceInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            method.getDeclaringClass();
            return null;
        }
    }

}
