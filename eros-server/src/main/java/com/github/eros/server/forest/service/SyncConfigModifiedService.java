package com.github.eros.server.forest.service;

import com.dtflys.forest.annotation.Request;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.github.eros.server.constant.ErosAppConstants;

public interface SyncConfigModifiedService {

    @Request(url = "http://" + "{serverDomain}" + ErosAppConstants.SERVER_SYNC_PATH, async = true)
    void sync(@Var("serverDomain")String nameServerDomain, @Var("namespace")String namespace,
              OnSuccess onSuccess, OnError onError);

}
