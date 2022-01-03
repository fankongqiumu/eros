package com.github.eros.common.cache;


import com.github.eros.common.lang.DefaultThreadFactory;
import com.github.eros.common.lang.Node;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MultiLocalCache<K, V> {

    private final CacheEngine<K, LinkedList<V>> cacheEngine;

    @SafeVarargs
    private MultiLocalCache(int maxSize, long expireAfterWrite, ExpireCallBack<K, V>... expireCallBacks){
        this.cacheEngine = new MultiExpireableWithLRUCache(maxSize, expireAfterWrite, expireCallBacks);
    }

    public List<V> get(K key) {
        return this.cacheEngine.get(key);
    }

    public boolean containsKey(K key) {
        return this.cacheEngine.containsKey(key);
    }

    public void remove(K key, V v){
        LinkedList<V> vs = this.cacheEngine.get(key);
        if (!vs.contains(v)){
            return;
        }
        synchronized (this.cacheEngine) {
            if (!vs.contains(v)){
                return;
            }
            vs.remove(v);
        }
    }

    public Long getlastModified(K key) {
        return null;
    }

    public void put(K key, V data) {
        this.cacheEngine.put(key, new LinkedList<>(Collections.singletonList(data)));
    }

    public int getSize() {
        return this.cacheEngine.getSize();
    }

    private class MultiExpireableWithLRUCache implements CacheEngine<K, LinkedList<V>> {
        private volatile LinkedHashMap<K, LinkedList<Node<K, V>>> cacheTabe;
        private final int maxSize;
        private final long expireAfterWrite;
        private final ExpireCallBack<K, V>[] expireCallBacks;
        private final ScheduledExecutorService scheduledExecutorService;

        private MultiExpireableWithLRUCache(int maxSize, long expireAfterWrite, ExpireCallBack<K, V>... expireCallBacks) {
            this.cacheTabe = new LinkedHashMap<>(maxSize);
            this.maxSize = maxSize;
            this.expireAfterWrite = expireAfterWrite;
            this.expireCallBacks = expireCallBacks;
            this.scheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                    DefaultThreadFactory.defaultThreadFactory("localCache-expire-thread")
            );
        }

        @Override
        public LinkedList<V> get(K key) {
            Deque<Node<K, V>> deque = cacheTabe.get(key);
            if (null != deque && !deque.isEmpty()) {
                Node<K, V> last = deque.peekLast();
                if (null != last && last.hasExpire(expireAfterWrite)) {
                    scheduledExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            innerSyncExpireOnly(last.getKey());
                        }
                    });
                }
            }
            LinkedList<V> list = new LinkedList<>();
            if (null != deque && !deque.isEmpty()) {
                for (Node<K, V> node : deque) {
                    list.add(node.getData());
                }
            }
            return list;
        }

        @Override
        public Long getlastModified(K key) {
            return null;
        }

        @Override
        public boolean containsKey(K key) {
            return cacheTabe.containsKey(key);
        }

        @Override
        public synchronized void put(K key, LinkedList<V> dataList) {
            if (null == dataList || dataList.isEmpty()) {
                return;
            }
            if (getSize() > maxSize) {
                Set<K> keySet = cacheTabe.keySet();
                for (K cacheKey : keySet) {
                    scheduledExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            innerSyncExpire(cacheKey);
                        }
                    });
                }
            }
            LinkedList<Node<K, V>> deque = cacheTabe.get(key);
            if (null == deque) {
                deque = new LinkedList<>();
            }
            long now = System.currentTimeMillis();
            for (V v : dataList) {
                Node<K, V> node = new Node<>(key, v, now);
                deque.offerFirst(node);
            }
            cacheTabe.put(key, deque);
        }

        @Override
        public int getSize() {
            return cacheTabe.size();
        }

        private void innerSyncExpire(K key) {
            if (expireAfterWrite <= 0 || getSize() <= maxSize){
                return;
            }
            synchronized (this) {
                while (getSize() > maxSize) {
                    if (getSize() <= maxSize) {
                        return;
                    }
                    LinkedList<Node<K, V>> nodes = cacheTabe.get(key);
                    if (null == nodes || nodes.isEmpty()) {
                        return;
                    }
                    Node<K, V> lastNode = nodes.pollLast();
                    if (null != expireCallBacks && expireCallBacks.length > 0) {
                        for (ExpireCallBack<K, V> expireCallBack : expireCallBacks) {
                            expireCallBack.callBack(key, lastNode.getData());
                        }
                    }
                    System.out.println("key：" + key + "data:" + lastNode.getData() + "expire...");
                }
            }
        }

        public void innerSyncExpireOnly(K key) {
            LinkedList<Node<K, V>> nodeLinkedList = cacheTabe.get(key);
            if (null == nodeLinkedList || nodeLinkedList.isEmpty()){
                return;
            }
            synchronized (this) {
                nodeLinkedList = cacheTabe.get(key);
                if (null == nodeLinkedList || nodeLinkedList.isEmpty()){
                    return;
                }
                Node<K, V> node = nodeLinkedList.peekLast();
                while (null != node && node.hasExpire(expireAfterWrite)) {
                    nodeLinkedList.pollLast();
                    if (null != expireCallBacks && expireCallBacks.length > 0) {
                        for (ExpireCallBack<K, V> expireCallBack : expireCallBacks) {
                            expireCallBack.callBack(key, node.getData());
                        }
                    }
                    System.out.println("key：" + key + "data:" + node.getData() + "expire...");
                    node = nodeLinkedList.peekLast();
                }
            }
        }
    }

    @SafeVarargs
    public static <K, V> MultiLocalCache<K, V> buildMultiLRULocalCache(int maxSize, long expireAfterWrite, ExpireCallBack<K, V>... expireCallBacks){
        return new MultiLocalCache<>(maxSize, expireAfterWrite, expireCallBacks);
    }
}
