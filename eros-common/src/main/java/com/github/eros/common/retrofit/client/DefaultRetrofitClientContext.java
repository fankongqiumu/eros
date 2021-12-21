package com.github.eros.common.retrofit.client;


import com.github.eros.common.lang.DefaultThreadFactory;
import com.github.eros.common.retrofit.annotation.RetrofitClientContext;
import okhttp3.Interceptor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 17:03
 */
public class DefaultRetrofitClientContext implements RetrofitClientContext {

    /**
     * 连接信息
     */
    private ServiceRequestAttribute serviceRequestAttribute;

    private ExecuteThreadPoolConfig executeThreadPoolConfig;

    private ConnectionPoolConfig connectionPoolConfig;

    private DefaultRetrofitClientContext() {
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
        return chain -> chain.proceed(chain.request());
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
