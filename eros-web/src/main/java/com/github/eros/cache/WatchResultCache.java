package com.github.eros.cache;

import com.github.eros.common.model.Result;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;

@Component
public class WatchResultCache {

    /**
     * guava中的Multimap，多值map,对map的增强，一个key可以保持多个value
     */
    private Multimap<String, DeferredResult<Result<Void>>> watchResults = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public void add(String namespace, DeferredResult<Result<Void>> deferredResult){
        watchResults.put(namespace, deferredResult);
    }

    public void remove(String namespace, DeferredResult<Result<Void>> deferredResult){
        watchResults.remove(namespace, deferredResult);
    }

    public boolean containNamespace(String namespace) {
        return watchResults.containsKey(namespace);
    }

    public Collection<DeferredResult<Result<Void>>> getByNameSpace(String namespace){
        return watchResults.get(namespace);
    }

}
