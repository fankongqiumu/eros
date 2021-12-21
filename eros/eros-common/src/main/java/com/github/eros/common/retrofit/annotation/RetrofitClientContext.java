package com.github.eros.common.retrofit.annotation;

import com.github.eros.common.retrofit.client.ConnectionPoolConfig;
import com.github.eros.common.retrofit.client.ExecuteThreadPoolConfig;
import com.github.eros.common.retrofit.client.ServiceRequestAttribute;
import okhttp3.Interceptor;

import com.github.eros.common.lang.NonNull;

/**
 * @author fankongqiumu
 * @description 子类必须有无参构造方法 用于反射
 * @date 2021/12/19 07:00
 */
public interface RetrofitClientContext {

    @NonNull
    ServiceRequestAttribute getServiceRequestAttribute();

    @NonNull
    Interceptor getInterceptor();

    @NonNull
    ExecuteThreadPoolConfig getExecuteThreadPoolConfig();

    @NonNull
    ConnectionPoolConfig getConnectionPoolConfig();
}
