package com.github.eros.async;

import com.github.eros.common.lang.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author fankongqiumu
 * @description Async 配置
 * @date 2021/12/22 14:32
 */
@Configuration
public class ErosAsyncConfigurer implements /*AsyncConfigurer,*/ WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ErosAsyncConfigurer.class);

//    private static final Executor executeService = new ThreadPoolExecutor(4, 10, 60L,
//            TimeUnit.SECONDS,
//            new LinkedBlockingQueue<>(2048),
//            DefaultThreadFactory.defaultThreadFactory("pool-eros-asyncExecutor-thread-"),
//            new ThreadPoolExecutor.DiscardPolicy()
//    );
//
//    @Bean(name = Constants.ASYNC_EXECUTOR)
//    @Override
//    public Executor getAsyncExecutor() {
//        return executeService;
//    }

    @Bean
    public ThreadPoolTaskExecutor mvcTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setQueueCapacity(100);
        executor.setMaxPoolSize(25);
        executor.setDaemon(true);
        return executor;
    }

    /**
     *  配置异步支持，设置了一个用来异步执行业务逻辑的工作线程池，设置了默认的超时时间是60秒
     * @param configurer
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcTaskExecutor());
        configurer.setDefaultTimeout(20000L);
    }

//    @Override
//    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//        return new AsyncUncaughtExceptionHandler(){
//            @Override
//            public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
//                logger.error("{} execute error:{}, method:{}, params:{}", Thread.currentThread().getName(), throwable, method, objects);
//            }
//        };
//    }


}