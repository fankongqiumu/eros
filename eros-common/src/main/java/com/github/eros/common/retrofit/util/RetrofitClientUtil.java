package com.github.eros.common.retrofit.util;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.model.Result;
import com.github.eros.common.util.ExceptionUtils;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

/**
 * @author fankongqiumu
 * @Description
 * @date {date}
 */
public class RetrofitClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(RetrofitClientUtil.class);

    private static final Integer DEFAULT_RETRY_COUNT = 1;

    public RetrofitClientUtil() {
    }

    public static <Target> Result<Target> executeCall(Call<Result<Target>> call){
        Response<Result<Target>> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            String traceId = ExceptionUtils.generateTraceId();
            logger.error("executeCall failed, trace:{}, request:{}, exception:{}", traceId,  call.request(), e);
            return  Result.createFailWith(ErosError.SYSTEM_ERROR.getCode(), "executeCall failed, trace:" + traceId);
        } finally {
            call.cancel();
        }
        if (!response.isSuccessful()) {
            String traceId = ExceptionUtils.generateTraceId();
            logger.error("executeCall failed, trace:{}, request:{}, response:{}", traceId,  call.request(), response);
            return  Result.createFailWith(ErosError.BUSINIESS_ERROR.getCode(), "executeCall failed, resCode: "+ response.code()+", resMsg: " + response.message() + ", trace:" + traceId);
        }
        Result<Target> result = response.body();
        if (result == null) {
            String traceId = ExceptionUtils.generateTraceId();
            logger.error("executeCall failed, http response body is null, trace:{}, request:{}, response:{}", traceId,  call.request(), response);
            return  Result.createFailWith(ErosError.BUSINIESS_ERROR.getCode(), "executeCall failed, http response body is null, trace:" + traceId);
        }
        return result;
    }

    public static <RetrofitService> RetrofitService getDefaultService(final OkHttpClient okHttpClient , final String baseUrl, final Class<RetrofitService> clazz) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CallAdapterFactory.create(DEFAULT_RETRY_COUNT))
                .client(okHttpClient)
                .build();
        return retrofit.create(clazz);
    }

}
