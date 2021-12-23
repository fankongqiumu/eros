package com.github.eros.client.forest.service;

import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import com.github.eros.client.forest.WatchAddressSource;
import com.github.eros.common.model.Result;

@Address(source = WatchAddressSource.class)
public interface FetchConfigService {

    @Get("/fetch/{namespace}")
    Result<String> watch(@Var("namespace") String namespace);

}
