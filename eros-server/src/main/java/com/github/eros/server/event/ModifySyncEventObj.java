package com.github.eros.server.event;

import com.github.eros.common.model.BaseObject;


public class ModifySyncEventObj extends BaseObject {
    private final String namespace;
    private final Long lastModified;

    public ModifySyncEventObj(String namespace, Long lastModified) {
        this.namespace = namespace;
        this.lastModified = lastModified;
    }

    public String getNamespace() {
        return namespace;
    }

    public Long getLastModified() {
        return lastModified;
    }
}
