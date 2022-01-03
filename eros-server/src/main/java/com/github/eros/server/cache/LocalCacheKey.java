package com.github.eros.server.cache;

public enum LocalCacheKey {
    SERVER_NODE_IP("localIp", String.class),
    SERVER_NODE_PORT("port", Integer.class),
    ;

    private String key;
    private Class<?> valClass;

    private LocalCacheKey(String key, Class<?> valClass){
        this.key = key;
        this.valClass = valClass;
    }

}
