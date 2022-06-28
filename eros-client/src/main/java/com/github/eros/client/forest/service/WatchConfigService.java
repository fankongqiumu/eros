package com.github.eros.client.forest.service;

import com.dtflys.forest.annotation.*;
import com.github.eros.client.forest.ForestFactory;
import com.github.eros.common.lang.Result;

@Address(source = ForestFactory.ClientAddressSource.class)
public interface WatchConfigService {

    /**
     * @param namespace todo head配置
     * headers[此处设置的header是服务端hold的时间]
     * @param namespace
     * @param clientKey
     * @return
     */
    @Get(url = "/async/watch/{namespace}")
    Result<Void> watch(@Var("namespace") String namespace,
                       @Query("clientKey")String clientKey,
                       @Query("timeout")Long timeout);
}
