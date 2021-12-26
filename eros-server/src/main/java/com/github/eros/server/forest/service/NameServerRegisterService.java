package com.github.eros.server.forest.service;


import com.dtflys.forest.annotation.Query;
import com.dtflys.forest.annotation.Request;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.github.nameserver.constant.UriConstants;

public interface NameServerRegisterService {
    @Request(url = "http://" + "{nameServerDomain}" + UriConstants.CLIENT_REGISTER_PATH, async = true)
    void register(@Var("nameServerDomain")String nameServerDomain, @Query("app")String app, @Query("serverDomain")String serverDomain, OnSuccess onSuccess, OnError onError);
}
