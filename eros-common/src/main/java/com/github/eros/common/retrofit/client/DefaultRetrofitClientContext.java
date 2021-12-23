package com.github.eros.common.retrofit.client;


import com.github.eros.common.lang.DefaultThreadFactory;
import com.github.eros.common.retrofit.annotation.RetrofitClientContext;
import com.github.eros.common.retrofit.intercept.Okhttp3Interceptor;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 17:03
 */
public class DefaultRetrofitClientContext implements RetrofitClientContext {

    /**
     * 目前是超时信息
     */
    protected ServiceRequestAttribute serviceRequestAttribute;

    protected ExecuteThreadPoolConfig executeThreadPoolConfig;

    protected ConnectionPoolConfig connectionPoolConfig;

    public DefaultRetrofitClientContext() {
    }


    @Override
    public ServiceRequestAttribute getServiceRequestAttribute() {
        if (null != this.serviceRequestAttribute) {
            return this.serviceRequestAttribute;
        }
        this.serviceRequestAttribute = ServiceRequestAttribute.getDefaultInstance();
        return this.serviceRequestAttribute;
    }

    @Override
    public Interceptor getInterceptor() {
        ServiceRequestAttribute serviceRequestAttribute = getServiceRequestAttribute();
        Long timeout = serviceRequestAttribute.getReadTimeout();
        Map<String, String> headers = serviceRequestAttribute.getHeaders();
        return chain -> {
            Request request = chain.request();
            if (null != headers && !headers.isEmpty()) {
                HttpUrl.Builder authorizedUrlBuilder = request.url()
                        .newBuilder()
                        .scheme(request.url().scheme())
                        .host(request.url().host());


                Request.Builder newBuilder = request.newBuilder()
                        .method(request.method(), request.body())
                        .url(authorizedUrlBuilder.build())
                        .addHeader("timeout", String.valueOf(timeout));
                // 如果有扩展则添加添加新的参数到header
                headers.forEach(newBuilder::addHeader);
                newBuilder.addHeader("Connection","keep-alive");
                newBuilder.addHeader("requestId", String.valueOf(System.nanoTime()));
                request = newBuilder.build();
            }
            // 新的请求
            return chain.proceed(request);
        };
    }

    @Override
    public ExecuteThreadPoolConfig getExecuteThreadPoolConfig() {
        if (null != this.executeThreadPoolConfig) {
            return this.executeThreadPoolConfig;
        }
        this.executeThreadPoolConfig = new ExecuteThreadPoolConfig();
        this.executeThreadPoolConfig.setCorePoolSize(4);
        this.executeThreadPoolConfig.setMaximumPoolSize(40);
        this.executeThreadPoolConfig.setKeepAliveTime(60L);
        this.executeThreadPoolConfig.setUnit(TimeUnit.SECONDS);
        this.executeThreadPoolConfig.setThreadFactory(DefaultThreadFactory.defaultThreadFactory("pool-eros-DefaultRetrofitClientDispatcher-thread-"));
        this.executeThreadPoolConfig.setWorkQueue(new LinkedBlockingQueue<>(2048));
        return this.executeThreadPoolConfig;
    }

    @Override
    public ConnectionPoolConfig getConnectionPoolConfig() {
        if (null != this.connectionPoolConfig) {
            return this.connectionPoolConfig;
        }
        this.connectionPoolConfig = new ConnectionPoolConfig();
        this.connectionPoolConfig.setMaxIdleConnections(8);
        this.connectionPoolConfig.setKeepAliveDuration(60L);
        this.connectionPoolConfig.setTimeUnit(TimeUnit.SECONDS);
        return this.connectionPoolConfig;
    }
}
