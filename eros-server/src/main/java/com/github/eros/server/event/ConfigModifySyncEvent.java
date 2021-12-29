package com.github.eros.server.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:55
 */
public class ConfigModifySyncEvent extends ApplicationEvent {

    private final ModifySyncEventObj modifySyncEventObj;

    public ConfigModifySyncEvent(final ModifySyncEventObj modifySyncEventObj) {
        super(modifySyncEventObj);
        this.modifySyncEventObj = modifySyncEventObj;
    }

    public String getNamespace() {
        return modifySyncEventObj.getNamespace();
    }

    public Long getLastModified() {
        return modifySyncEventObj.getLastModified();
    }
}
