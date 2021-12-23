package com.github.eros.common.retrofit.client;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 17:08
 */
public class ServiceRequestAttribute {
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private Long connectTimeout = 300000L;
    private Long readTimeout = 300000L;
    private Long writeTimeout = 300000L;
    private Map<String, String> headers;


    public ServiceRequestAttribute() {
    }

    public static ServiceRequestAttribute getDefaultInstance() {
        return new ServiceRequestAttribute();
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public Long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
