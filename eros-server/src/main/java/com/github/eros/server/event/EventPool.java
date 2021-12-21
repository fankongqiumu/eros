package com.github.eros.server.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:58
 */
public class EventPool {

    private static final Map<Class<? extends Event>, Set<EventListener<? extends Event>>> EVENT_LISTENER_POOL = new ConcurrentHashMap<>(64);

    public static synchronized <TargetEvent extends Event>  void register(Class<TargetEvent> eventType, EventListener<TargetEvent> listener) {
        if (null != eventType && null != listener) {
            Set<EventListener<?>> listeners = Optional.ofNullable(EVENT_LISTENER_POOL.get(eventType)).orElse(new HashSet<>(64));
            listeners.add(listener);
            EVENT_LISTENER_POOL.put(eventType, listeners);
        }
    }

    public static Set<EventListener<? extends Event>> getListerners(Class<? extends Event> eventType) {
        return EVENT_LISTENER_POOL.get(eventType);
    }

}
