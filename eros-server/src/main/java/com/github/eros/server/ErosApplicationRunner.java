package com.github.eros.server;

import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.server.cache.ConfigLocalCache;
import com.github.eros.server.forest.ForestFactory;
import com.github.eros.server.forest.service.NameServerRegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

@Component
public class ErosApplicationRunner implements InitializingBean, ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLocalCache.class);

    private static final String APP = "EROS_SERVER";

    @Value("${nameserver.domain}")
    private String nameServerDomain;

    @Value("${server.port}")
    private String port;

    private String localIp;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        NameServerRegisterService nameServerRegisterService = ForestFactory.createInstance(NameServerRegisterService.class);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final String serverDomain = localIp + ":" + port;
        final ForestRuntimeException[] forestRuntimeException = {null};
        nameServerRegisterService.register(nameServerDomain, APP, serverDomain, new OnSuccess() {
            @Override
            public void onSuccess(Object data, ForestRequest req, ForestResponse res) {
                logger.info("register this server [{}] serverDomain [{}] to nameserver [{}] success...", APP, serverDomain, nameServerDomain);
                countDownLatch.countDown();
            }
        }, new OnError() {
            @Override
            public void onError(ForestRuntimeException ex, ForestRequest req, ForestResponse res) {
                logger.info("register this server [{}] serverDomain [{}] to nameserver [{}] error...", APP, serverDomain, nameServerDomain);
                countDownLatch.countDown();
                forestRuntimeException[0] = ex;
            }
        });
        countDownLatch.await();
        if (null != forestRuntimeException[0]){
            throw new ErosException(ErosError.SYSTEM_ERROR, forestRuntimeException[0]);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new ErosException(ErosError.SYSTEM_ERROR, "InetAddress.getLocalHost().getHostAddress() error", e);
        }
    }
}
