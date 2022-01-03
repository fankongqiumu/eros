package com.github.eros.common.cache;

public interface ExpireCallBack<K, V> {
    void callBack(K key, V data);
}
