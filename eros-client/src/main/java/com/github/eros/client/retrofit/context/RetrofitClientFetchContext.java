package com.github.eros.client.retrofit.context;

import com.github.eros.common.retrofit.client.DefaultRetrofitClientContext;
import com.github.eros.common.retrofit.client.ServiceRequestAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RetrofitClientFetchContext extends DefaultRetrofitClientContext {

    @Override
    public ServiceRequestAttribute getServiceRequestAttribute() {
        if (null != this.serviceRequestAttribute) {
            return this.serviceRequestAttribute;
        }
        ServiceRequestAttribute serviceRequestAttribute = ServiceRequestAttribute.getDefaultInstance();
        // 客户端超时时长
        serviceRequestAttribute.setUnit(TimeUnit.MILLISECONDS);
        serviceRequestAttribute.setReadTimeout(300000L);
        Map<String, String> headers = Optional.ofNullable(serviceRequestAttribute.getHeaders()).orElse(new HashMap<>());
        // 这个值是为服务端处理长轮训设置的
        headers.put("timeout", String.valueOf(serviceRequestAttribute.getReadTimeout()));
        serviceRequestAttribute.setHeaders(headers);
        this.serviceRequestAttribute = serviceRequestAttribute;
        return this.serviceRequestAttribute;
    }
}
