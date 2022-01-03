package com.github.eros.common.cache;

public interface CacheEngine<K, V> {
    V get(K key);

    Long getlastModified(K key);

    boolean containsKey(K key);

    void put(K key, V data);

    default void putPlus(K key, V data, long lastModified){
        put(key, data);
    }

    default void extPut(K key, V data) {
        put(key, data);
    }

    int  getSize();
}
