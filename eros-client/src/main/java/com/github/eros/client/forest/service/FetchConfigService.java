package com.github.eros.client.forest.service;

import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import com.github.eros.client.forest.ForestFactory;
import com.github.eros.common.lang.Result;
import com.github.eros.common.model.Config;

@Address(source = ForestFactory.ClientAddressSource.class)
public interface FetchConfigService {

    /**
     * fetch namespace 的配置
     * @param namespace todo head配置
     * @return
     */
    @Get(
            url = "/fetch/{namespace}",
            dataType = "json"
    )
    Result<Config> fetch(@Var("namespace") String namespace);

}
