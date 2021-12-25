package com.github.eros.server.cache;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:58
 */
@Component
public class EventCache {

    private static final Map<String, ApplicationEvent> EVENT_LISTENER_POOL = new HashMap<>(128);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final  ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    public void register(String key, ApplicationEvent event) {
        if (EVENT_LISTENER_POOL.containsKey(key)){
            return;
        }
        writeLock.lock();
        try {
            EVENT_LISTENER_POOL.put(key, event);
        } finally {
            writeLock.unlock();
        }
    }

    public <Event extends ApplicationEvent> Event getEvent(String key, Class<Event> eventType) {
        if (!EVENT_LISTENER_POOL.containsKey(key)){
            return null;
        }
        writeLock.lock();
        try {
            ApplicationEvent event = EVENT_LISTENER_POOL.get(key);
            EVENT_LISTENER_POOL.remove(key);
            return (Event) event;
        } finally {
            writeLock.unlock();
        }
    }

}
