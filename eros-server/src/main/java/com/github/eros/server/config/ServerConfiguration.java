package com.github.eros.server.config;

import com.github.eros.common.cache.LocalCache;
import com.github.eros.common.lang.DefaultThreadFactory;
import com.github.eros.server.common.Constants;
import com.github.eros.server.event.ConfigModifySyncEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;


@Configuration
public class ServerConfiguration {

    @Autowired
    private Environment environment;

    private static final int MAX_POOL_SIZE = 50;

    private static final int CORE_POOL_SIZE = 20;

    @Bean(Constants.ExecutorConstants.ASYNC_EVENT_TASK_EXECUTOR)
    public AsyncTaskExecutor asyncEventTaskExecutor() {
        ThreadPoolTaskExecutor asyncTaskExecutor = new ThreadPoolTaskExecutor();
        asyncTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        asyncTaskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        asyncTaskExecutor.setThreadNamePrefix("async-event-publish-task-");
        asyncTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        asyncTaskExecutor.initialize();
        return asyncTaskExecutor;
    }

    @Bean(name = Constants.CacheConstants.CONFIG_MODIFY_SYNC_EVENT_CACHE)
    public LocalCache<String, ConfigModifySyncEvent> configModifySyncEventCache() {
        return LocalCache.buildExpireableCache(1024, 15000L);
    }

    @Bean(Constants.ExecutorConstants.MODIFIED_SYNC_DISPATCHER_SERVICE)
    public ExecutorService modifiedSyncDispatcherService(){
        return new ThreadPoolExecutor(1, 10,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue(1024),
                DefaultThreadFactory.defaultThreadFactory("pool-eros-modifiedSync-thread-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Bean(name = "modifiedSyncScheduledService")
    public ScheduledExecutorService modifiedSyncScheduledService(){
        return  new ScheduledThreadPoolExecutor(5
                , DefaultThreadFactory.defaultThreadFactory("pool-eros-watch-thread-")
        );
    }

}