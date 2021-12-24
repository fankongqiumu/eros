package com.github.eros.client.forest.service;

import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import com.github.eros.client.forest.ClientAddressSource;
import com.github.eros.common.model.Result;
import com.github.eros.domain.Config;

@Address(source = ClientAddressSource.class)
public interface FetchConfigService {

    /**
     * fetch namespace 的配置
     * @param namespace
     * @return
     */
    @Get(
            url = "/fetch/{namespace}",
            dataType = "json"
    )
    Result<Config> fetch(@Var("namespace") String namespace);

}
