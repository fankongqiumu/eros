package com.github.eros.server.event;

import java.util.Set;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:40
 */
public interface EventPublisher<TargetEvent extends Event> {

    /**
     * 默认的事件发布
     * @param targetEvent
     */
    default void publishEvent(TargetEvent targetEvent) {
        Set<EventListener<? extends Event>> listeners = getListeners(targetEvent);
        for (EventListener<? extends Event> listener : listeners) {
            try {
                EventListener<TargetEvent> targetEventListener = (EventListener<TargetEvent>)listener;
                targetEventListener.onEvent(targetEvent);
            } catch (Exception e) {
            }
        }
    }

    default Set<EventListener<? extends Event>> getListeners(TargetEvent targetEvent) {
       return EventPool.getListerners(targetEvent.getClass());
    }
}
