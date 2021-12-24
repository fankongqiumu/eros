package com.github.eros.client.forest.service;

import com.dtflys.forest.annotation.*;
import com.github.eros.client.forest.ClientAddressSource;
import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.model.Result;

@Address(source = ClientAddressSource.class)
public interface WatchConfigService {

    /**
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
