package com.github.eros.common.cache;

public interface Expireable<K> {
    void expire(K key);
}
