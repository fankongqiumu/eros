package com.github.eros.server.cache;

import java.util.List;

public enum LocalCacheKey {
    SERVER_NODE_IP("localIp", String.class),
    SERVER_NODE_PORT("port", Integer.class),
    SERVER_NODE_LIST("serverNodeList", List.class),
    ;

    private String key;
    private Class<?> valClass;

    private LocalCacheKey(String key, Class<?> valClass){
        this.key = key;
        this.valClass = valClass;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getValClass() {
        return valClass;
    }
}
