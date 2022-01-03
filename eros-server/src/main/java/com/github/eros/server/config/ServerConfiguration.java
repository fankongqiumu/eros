package com.github.eros.server.config;

import com.github.eros.common.cache.LocalCache;
import com.github.eros.server.event.ConfigModifySyncEvent;
import com.github.nameserver.NameServerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
public class ServerConfiguration {

    @Value("${nameserver.domain}")
    private String nameServerDomain;


    private static final int MAX_POOL_SIZE = 50;

    private static final int CORE_POOL_SIZE = 20;

    @Bean("asyncEventTaskExecutor")
    public AsyncTaskExecutor asyncEventTaskExecutor() {
        ThreadPoolTaskExecutor asyncTaskExecutor = new ThreadPoolTaskExecutor();
        asyncTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        asyncTaskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        asyncTaskExecutor.setThreadNamePrefix("async-event-publish-task-");
        asyncTaskExecutor.initialize();
        return asyncTaskExecutor;
    }

    @Bean(name = "configModifySyncEventCache")
    public LocalCache<String, ConfigModifySyncEvent> configModifySyncEventCache() {
        return LocalCache.buildExpireableCache(1024, 15000L);
    }

//    @Bean(name = "nameServerClient")
//    public NameServerClient nameServerClient(){
//        return NameServerClient.getInstance(nameServerDomain, true);
//    }



}
