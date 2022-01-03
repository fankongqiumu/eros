package com.github.eros.cache;

import com.github.eros.common.cache.ExpireCallBack;
import com.github.eros.common.cache.MultiLocalCache;
import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.lang.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@Component
public class WatchResultCache {

    private final MultiLocalCache<String, DeferredResult<Result<Void>>> watchResults = MultiLocalCache.buildMultiLRULocalCache(
            2048, 60000L, new ExpireCallBack<String, DeferredResult<Result<Void>>>() {
                @Override
                public void callBack(String key, DeferredResult<Result<Void>> data) {
                    // todo 让客户端去连接负载较小的服务器
                    Result<Void> noContent = Result.createSuccess();
                    noContent.setMsgCode(String.valueOf(HttpConstants.HttpStatus.SERVER_BUSY.getCode()));
                    noContent.setMsgInfo(HttpConstants.HttpStatus.SERVER_BUSY.getReasonPhrase());
                    data.setResult(noContent);
                }
            });

    public void add(String namespace, DeferredResult<Result<Void>> deferredResult){
        watchResults.put(namespace, deferredResult);
    }

    public void remove(String namespace, DeferredResult<Result<Void>> deferredResult){
       watchResults.remove(namespace, deferredResult);
    }

    public boolean containNamespace(String namespace) {
        return watchResults.containsKey(namespace);
    }

    public List<DeferredResult<Result<Void>>> getByNameSpace(String namespace){
        return watchResults.get(namespace);
    }

}
