package com.github.eros.server;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.server.cache.ErosServerLocalCache;
import com.github.eros.server.cache.LocalCacheKey;
import com.github.eros.server.constant.ErosAppConstants;
import com.github.nameserver.NameServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ErosApplicationRunner implements InitializingBean, ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${server.port}")
    private int port;

    private String localIp;

    @Autowired
    private NameServerClient nameServerClient;

    @Autowired
    private ErosServerLocalCache erosServerLocalCache;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        nameServerClient.register(ErosAppConstants.DEFAULT_APP_NAME, localIp, port);
        erosServerLocalCache.putObject(LocalCacheKey.SERVER_NODE_IP, localIp);
        erosServerLocalCache.putObject(LocalCacheKey.SERVER_NODE_PORT, port);
    }

    @Override
    public void afterPropertiesSet() throws Exception{
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new ErosException(ErosError.SYSTEM_ERROR, "InetAddress.getLocalHost().getHostAddress() error", e);
        }
    }

}
