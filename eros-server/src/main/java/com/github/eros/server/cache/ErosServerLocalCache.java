package com.github.eros.server.cache;

import com.github.eros.common.cache.LocalCache;
import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class ErosServerLocalCache implements InitializingBean {

    private LocalCache<LocalCacheKey, Object> localCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        localCache = LocalCache.buildSimpleSynCache(256);
    }

    public Object getObject(LocalCacheKey cacheKey){
        return localCache.get(cacheKey);
    }

    public String getString(LocalCacheKey cacheKey){
        Object obj = getObject(cacheKey);
        if (null == obj){
            return "";
        }
        if (obj instanceof String){
            return (String) obj;
        }
        return obj.toString();
    }

    public Long getLong(LocalCacheKey cacheKey){
        Object obj = getObject(cacheKey);
        if (null == obj){
            return null;
        }
        if (obj instanceof Long){
            return (Long) obj;
        }
        if (obj instanceof String && StringUtils.isNumeric((String) obj)){
            return Long.valueOf((String) obj);
        }
        throw new ErosException(ErosError.BUSINIESS_ERROR, String.format("required long but target is %s, cannot transfer...",  obj.getClass().getName()));
    }

    public Integer getInteger(LocalCacheKey cacheKey){
        Object obj = getObject(cacheKey);
        if (null == obj){
            return null;
        }
        if (obj instanceof Integer){
            return (Integer) obj;
        }
        if (obj instanceof String && StringUtils.isNumeric((String) obj)){
            return Integer.valueOf((String) obj);
        }
        throw new ErosException(ErosError.BUSINIESS_ERROR, String.format("required Integer but target is %s, cannot transfer...",  obj.getClass().getName()));
    }


    public void putObject(LocalCacheKey cacheKey, Object obj){
        if (null == obj){
            throw new ErosException(ErosError.BUSINIESS_ERROR, "obj is null");
        }
        if (null == cacheKey){
            throw new ErosException(ErosError.BUSINIESS_ERROR, "cacheKey is null");
        }
        Class<?> keyValClass = cacheKey.getValClass();
        if (!keyValClass.isInstance(obj)){
            throw new ErosException(ErosError.BUSINIESS_ERROR, String.format("required %s not %s", keyValClass.getName(),  obj.getClass().getName()));
        }
        localCache.put(cacheKey, obj);
    }

}
