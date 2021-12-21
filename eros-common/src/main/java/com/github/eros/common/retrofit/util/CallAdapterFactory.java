package com.github.eros.common.retrofit.util;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author fankongqiumu
 * @Description
 * @date {date}
 */
public class CallAdapterFactory extends CallAdapter.Factory {

    private int retryCount;

    public static CallAdapter.Factory create(int retryCount) {
        return new CallAdapterFactory(retryCount);
    }

    private CallAdapterFactory() {}

    private CallAdapterFactory(int retryCount) {
        this.retryCount = Math.max(1, retryCount);
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call.class) {
            return null;
        }

        final Type responseType = getCallResponseType(returnType);
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return new HttpCall<>(call, retryCount);
            }
        };
    }

    private Type getCallResponseType(Type returnType) {
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
        }
        return getParameterUpperBound(0, (ParameterizedType)returnType);
    }

    static class HttpCall<T> implements Call<T> {

        private final int retry;
        private Call<T> call;

        HttpCall(Call<T> call, int retry) {
            this.call = call;
            this.retry = retry;
        }

        @Override
        public Response<T> execute() throws IOException {
            int retry = this.retry;
            Exception exception = null;
            Response<T> errResponse = null;
            while (retry > 0) {
                Response<T> response;
                try {
                    if (call.isExecuted()) {
                        call = call.clone();
                    }
                    response = call.execute();
                } catch (Exception e) {
                    retry--;
                    exception = e;
                    errResponse = null;
                    continue;
                }
                if (response.isSuccessful()) {
                    return response;
                } else {
                    retry--;
                    errResponse = response;
                    exception = null;
                }
            }

            if (exception != null) {
                String errMsg = String.format("http %s exception", call.request().toString());
                throw new IOException(errMsg, exception);
            } else {
                return errResponse;
            }
        }

        @Override
        public void enqueue(Callback<T> callback) {
            call.enqueue(callback);
        }

        @Override
        public boolean isExecuted() {
            return call.isExecuted();
        }

        @Override
        public void cancel() {
            call.cancel();
        }

        @Override
        public boolean isCanceled() {
            return call.isCanceled();
        }

        @Override
        public Call<T> clone() {
            return new HttpCall<>(call.clone(), retry);
        }

        @Override
        public Request request() {
            return call.request();
        }
    }

}
