package com.github.eros.retrofit;

import com.github.eros.common.model.Result;
import com.github.eros.common.retrofit.annotation.EnableRetrofitService;
import com.github.eros.common.retrofit.client.RetrofitClient;
import com.github.eros.common.retrofit.util.RetrofitClientUtil;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author fankongqiumu
 * @description TODO
 * @date 2021/12/19 21:25
 */
public class RetrofitServiceDemo {

    public static void main(String[] args) {
        RetrofitClient client = RetrofitClient.Builder.build(DemoService.class);
        DemoService demoService = client.getRetrofitServiceInstance(DemoService.class);
        Result<String> result = RetrofitClientUtil.executeCall(demoService.demo());
        System.out.println(result);
    }


    @EnableRetrofitService(baseUrl = "http://127.0.0.1:8080")
    public interface DemoService {

        /**
         * 测试接口
         * @return return
         */
        @GET("/test/demo")
        Call<Result<String>> demo();
    }

}
