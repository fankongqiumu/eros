package com.github.eros.common.retrofit.client;

import java.util.concurrent.TimeUnit;

/**
 * @author fankongqiumu
 * @description TODO
 * @date 2021/12/19 07:05
 */
public class ConnectionPoolConfig {
    private Integer maxIdleConnections;
    private Long keepAliveDuration;
    private TimeUnit timeUnit;

    public Integer getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(Integer maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public Long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(Long keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

}
