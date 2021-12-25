package com.github.eros.server.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:55
 */
public class ConfigModifySyncEvent extends ApplicationEvent {

    private final String namespace;

    public ConfigModifySyncEvent(String namespace) {
        super(namespace);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
