package com.github.eros.client.forest.service;

import com.dtflys.forest.annotation.*;
import com.github.eros.client.forest.WatchAddressSource;
import com.github.eros.common.model.Result;

@Address(source = WatchAddressSource.class)
public interface WatchConfigService {

    // 此处设置的header是服务端hold的时间
    @Get(url = "/async/watch/{namespace}", headers = {"timeout: 60000"})
    Result<Void> watch(@Var("namespace") String namespace);
}
