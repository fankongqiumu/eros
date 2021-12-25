package com.github.eros.server.cache;

import com.github.eros.common.lang.MD5;
import com.github.eros.domain.Config;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class ConfigLocalCache {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLocalCache.class);

    private static final LoadingCache<String, Config> cache = CacheBuilder.newBuilder()
            //设置并发级别为8，并发级别是指可以同时写缓存的线程数
            .concurrencyLevel(8)
            //设置缓存容器的初始容量为10
            .initialCapacity(128)
            //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
            .maximumSize(1024)
            //是否需要统计缓存情况,该操作消耗一定的性能,生产环境应该去除
//            .recordStats()
            //设置写缓存后n秒钟过期
            .expireAfterWrite(17, TimeUnit.SECONDS)
            //设置读写缓存后n秒钟过期,实际很少用到,类似于expireAfterWrite
            //.expireAfterAccess(17, TimeUnit.SECONDS)
            //只阻塞当前数据加载线程，其他线程返回旧值
            //.refreshAfterWrite(13, TimeUnit.SECONDS)
            //设置缓存的移除通知
            .removalListener(notification -> {
                logger.info(notification.getKey() + " " + notification.getValue() + " 被移除,原因:" + notification.getCause());
            })
            //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
            .build(new DemoCacheLoader());


    public Config get(String namespace) {
        try {
            return cache.get(namespace);
        } catch (ExecutionException e) {
            logger.error("get [{}] config failed, cause:{}", namespace, e);
        }
        return null;
    }

    public void refresh(String namespace){
        cache.refresh(namespace);
    }

    /**
     * 随机缓存加载,实际使用时应实现业务的缓存加载逻辑,例如从数据库获取数据
     */
    public static class DemoCacheLoader extends CacheLoader<String, Config> {
        @Override
        public Config load(String key) throws Exception {
            System.out.println(Thread.currentThread().getName() + " 加载数据开始");
            TimeUnit.SECONDS.sleep(8);
            logger.info(Thread.currentThread().getName() + " 加载数据结束");
            // todo 当前mock， 待实现
            Config config = new Config();
            config.setNamespace(key);
            config.setData("{}");
            config.setLastModified(System.currentTimeMillis());
            config.setCheckMd5(MD5.getInstance().getMD5(config.getData()));
            return config;
        }
    }
}
