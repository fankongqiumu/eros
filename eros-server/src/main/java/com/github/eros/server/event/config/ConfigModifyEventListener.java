package com.github.eros.server.event.config;

import com.github.eros.server.event.EventListener;
import com.github.eros.server.event.EventPool;
import org.springframework.stereotype.Component;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:53
 */
@Component
public class ConfigModifyEventListener implements EventListener<ConfigModifyEvent> {

    public ConfigModifyEventListener() {
        EventPool.register(ConfigModifyEvent.class, this);
    }

    @Override
    public void onEvent(ConfigModifyEvent event) {
        // todo do something
    }

}
