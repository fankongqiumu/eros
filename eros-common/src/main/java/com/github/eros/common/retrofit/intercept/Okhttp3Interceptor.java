package com.github.eros.common.retrofit.intercept;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;

public class Okhttp3Interceptor implements okhttp3.Interceptor {


    @Override
    public Response intercept(okhttp3.Interceptor.Chain chain) throws IOException {
        Request oldRequest = chain.request();

        // 添加新的参数
        HttpUrl.Builder authorizedUrlBuilder = oldRequest.url()
            .newBuilder()
            .scheme(oldRequest.url().scheme())
            .host(oldRequest.url().host());



        // 新的请求
        Request newRequest = oldRequest.newBuilder()
            .method(oldRequest.method(), oldRequest.body())
            .url(authorizedUrlBuilder.build())
            .build();

        return chain.proceed(newRequest);
    }
}