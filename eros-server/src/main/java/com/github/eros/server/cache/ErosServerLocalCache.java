package com.github.eros.server.cache;

import com.github.eros.common.cache.LocalCache;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class ErosServerLocalCache implements InitializingBean {

    private LocalCache<LocalCacheKey, Object> localCache;

    public Object getObject(LocalCacheKey cacheKey){
        return localCache.get(cacheKey);
    }

    public void putObject(LocalCacheKey cacheKey, Object obj){
        localCache.put(cacheKey, obj);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        localCache = LocalCache.buildSimpleSynCache(256);
    }
}
