package com.github.eros.client.retrofit.service;

import com.github.eros.client.retrofit.context.RetrofitClientFetchContext;
import com.github.eros.client.retrofit.entry.ConfigInfo;
import com.github.eros.common.lang.NonNull;
import com.github.eros.common.model.Result;
import com.github.eros.common.retrofit.annotation.EnableRetrofitService;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 01:45
 */
@EnableRetrofitService(context = RetrofitClientFetchContext.class, baseUrl = ConfigService.SERVER_DOMAIN)
@FunctionalInterface
public interface ConfigService {

    String SERVER_DOMAIN = "http://eros.test.b2c.srv";

    /**
     * 客户端http方式获取配置
     * @param namespace
     * @param app
     * @param group
     * @return
     */
    @GET("/async/watch/{namespace}")
    Call<Result<ConfigInfo>> watch(@NonNull @Path("namespace") String namespace,
                                   @NonNull @Query("app") String app,
                                   @NonNull @Query("group") String group
    );
}