package com.github.eros.server.event;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:51
 */
public interface EventListener<TargetEvent extends Event> {
    void onEvent(TargetEvent targetEvent);
}