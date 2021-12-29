package com.github.eros.server.cache;

import com.github.eros.common.lang.DefaultThreadFactory;

import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalCache<K, V> {

    private final CacheEngine<K, V> cacheEngine;

    private LocalCache(int initialCapacity, boolean sync, boolean lru, long expireAfterWrite) {
        if (lru){
            this.cacheEngine = new ExpireableWithLRUCache(initialCapacity, Long.MIN_VALUE);
        } else if (sync && expireAfterWrite > 0L){
            this.cacheEngine = new ExpireableCache(initialCapacity, expireAfterWrite);
        } else if (sync) {
            this.cacheEngine = new SimpleSynCache(initialCapacity);
        } else {
            this.cacheEngine = new SimpleCache(initialCapacity);
        }
    }


    public V get(K key) {
        return this.cacheEngine.get(key);
    }

    /**
     * ExpireableCache和ExpireableWithLRUCache引擎有意义
     * 其他引擎默认返回null
     * @param key
     * @return
     */
    public Long getlastModified(K key){
        return this.cacheEngine.getlastModified(key);
    }

    public void put(K key, V data) {
        this.cacheEngine.put(key, data);
    }

    /**
     * ExpireableCache引擎有意义, 其他引擎行为同 put
     * @param key
     * @param data
     * @param lastModified
     */
    public void putPuls(K key, V data, long lastModified) {
        this.cacheEngine.putPlus(key, data, lastModified);
    }

    public int getSize() {
        return this.cacheEngine.getSize();
    }

    public static <K, V> LocalCache<K, V> buildSimpleCache(int initialCapacity, Class<K> classOfKey, Class<V> classOfData){
        return new LocalCache<>(initialCapacity, false, false, Long.MIN_VALUE);
    }

    public static <K, V> LocalCache<K, V> buildSimpleSynCache(int initialCapacity, Class<K> classOfKey, Class<V> classOfData){
        return new LocalCache<>(initialCapacity, true, false, Long.MIN_VALUE);
    }

    public static <K, V> LocalCache<K, V> buildExpireableCache(int initialCapacity, long expireAfterWrite,  Class<K> classOfKey, Class<V> classOfData) {
        return new LocalCache<>(initialCapacity, true, false, expireAfterWrite);
    }

    public static <K, V> LocalCache<K, V> buildLRUCache(int initialCapacity,  Class<K> classOfKey, Class<V> classOfData) {
        return new LocalCache<>(initialCapacity, true, true, Long.MIN_VALUE);
    }

    private interface CacheEngine<K, V> {
        V get(K key);

        Long getlastModified(K key);

        void put(K key, V data);

        default void putPlus(K key, V data, long lastModified){
            put(key, data);
        }

        int  getSize();
    }

    private class SimpleCache implements CacheEngine<K, V>{

        protected final LinkedHashMap<K, V> cacheTabe;

        private SimpleCache(int initialCapacity){
            cacheTabe = new LinkedHashMap<>(initialCapacity);
        }

        @Override
        public V get(K key) {
            return cacheTabe.get(key);
        }

        @Override
        public Long getlastModified(K key) {
            return null;
        }


        @Override
        public void put(K key, V data) {
            cacheTabe.put(key, data);
        }

        @Override
        public int getSize() {
            return cacheTabe.size();
        }
    }

    private class SimpleSynCache extends SimpleCache{
        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

        private SimpleSynCache(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public V get(K key) {
            readLock.lock();
            try {
                return super.get(key);
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public void put(K key, V data) {
            writeLock.lock();
            try {
                super.put(key, data);
            } finally {
                writeLock.unlock();
            }
        }

        @Override
        public int getSize() {
            return super.getSize();
        }

    }

    private class ExpireableCache implements CacheEngine<K, V>, Runnable{
        protected final LinkedHashMap<K, Node> cacheTabe;
        protected final DoubleList cacheDoubleList;
        /**
         * 过期时间 TimeUnit.MILLISECONDS
         */
        protected final long expireAfterWrite;

        protected final ScheduledExecutorService scheduledExecutorService;
        protected final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        protected final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        protected final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        protected final Condition condition =  writeLock.newCondition();;

        /**
         *
         * @param initialCapacity 暂时没啥用
         * @param expireAfterWrite 小于等于0L 表示不需要过期， 大于0会在指定毫秒值后过期
         */
        public ExpireableCache(int initialCapacity, long expireAfterWrite) {
            this.cacheTabe = new LinkedHashMap<>(initialCapacity);
            this.cacheDoubleList = new DoubleList();
            this.expireAfterWrite = expireAfterWrite;
            if (expireAfterWrite <= 0L) {
                scheduledExecutorService = null;
            } else {
                scheduledExecutorService = new ScheduledThreadPoolExecutor(2,
                        DefaultThreadFactory.defaultThreadFactory("localCache-expire-thread")
                );
                scheduledExecutorService.scheduleAtFixedRate(this, 1, 10, TimeUnit.SECONDS);
            }
        }

        @Override
        public V get(K key) {
            Node node = cacheTabe.get(key);
            if (null != node) {
                if (node.hasExpire()) {
                    expire(key);
                    return null;
                }
                return node.data;
            }
            return null;
        }

        @Override
        public Long getlastModified(K key){
            Node node = cacheTabe.get(key);
            if (null != node) {
                if (node.hasExpire()) {
                    expire(key);
                    return null;
                }
                return node.lastModified;
            }
            return null;
        }

        /**
         * 覆盖式写缓存，如果存在相应key的缓存会覆盖原来的值且延长过期时间
         *
         * @param key
         * @param data
         */
        @Override
        public void put(K key, V data) {
            innerSyncCache(key, data, System.currentTimeMillis());
        }

        /**
         * 自定义最后修改时间，如果缓存中存在更新时间大于传入的 lastModified 则会放弃此次修改
         *
         * @param key
         * @param data
         * @param lastModified
         */
        @Override
        public void putPlus(K key, V data, long lastModified) {
            readLock.lock();
            try {
                Node node = cacheTabe.get(key);
                if (null != node && !node.hasExpire(lastModified)) {
                    return;
                }
            } finally {
                readLock.unlock();
            }
            innerSyncCache(key, data, lastModified);
        }

        @Override
        public int getSize() {
            return cacheDoubleList.size;
        }

        private void innerSyncCache(K key, V data, Long lastModified) {
            if (cacheTabe.containsKey(key)) {
                innerSyncExpire(key);
            }
            writeLock.lock();
            try {
                Node node = new Node(key, data, lastModified);
                node = cacheDoubleList.addFirst(node);
                cacheTabe.put(node.key, node);
            } finally {
                checkAndSignalAllWaitCondition();
                writeLock.unlock();
            }
        }

        private void expire(K key) {
            if (expireAfterWrite <= 0){
                return;
            }
            scheduledExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    readLock.lock();
                    try {
                        if (null == cacheTabe.get(key)) {
                            return;
                        }
                    } finally {
                        readLock.unlock();
                    }
                    innerSyncExpire(key);
                }
            });
        }

        private void innerSyncExpire(K key) {
            if (expireAfterWrite <= 0){
                return;
            }
            writeLock.lock();
            try {
                Node originNode = cacheTabe.get(key);
                if (null == originNode){
                    return;
                }
                cacheTabe.remove(key);
                cacheDoubleList.remove(originNode);
            } finally {
                writeLock.unlock();
            }
        }

        private void checkAndSignalAllWaitCondition(){
            if (readWriteLock.hasWaiters(condition)) {
                condition.signalAll();
            }
        }

        @Override
        public void run() {
            if (expireAfterWrite <= 0L || readWriteLock.isWriteLocked()){
                return;
            }
            writeLock.lock();
            try{
                final int size = this.getSize();
                try {
                    if (size == 0) {
                        condition.await();
                    }
                } catch (InterruptedException e) {
                }
                Node last = cacheDoubleList.getLast();
                while (null != last) {
                    if (System.currentTimeMillis() - last.lastModified > this.expireAfterWrite) {
                        innerSyncExpire(last.key);
                        last = cacheDoubleList.getLast();
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }

        class DoubleList {
            private Node head;
            private Node tail;
            private int size;

            // 在链表头部添加节点 node，时间 O(1)
            public Node addFirst(Node node) {
                if (null == head) {
                    tail = node;
                } else {
                    node.next = head;
                    head.prev = node;
                }
                head = node;
                size++;
                return head;
            }

            public Node getFirst() {
                return head;
            }

            // 删除链表中的 node 节点（x 一定存在）
            // 由于是双链表且给的是目标 Node 节点，时间 O(1)
            public void remove(Node node) {
                if (null == node) {
                    return;
                }
                Node prev = node.prev;
                Node next = node.next;
                if (null == prev) {
                    // prev == null 说明第head节点
                    // 删除头结点 需要改变头结点指针
                    head = next;
                } else {
                    prev.next = next;
                }
                if (null == next) {
                    // 说明是tail节点
                    tail = prev;
                } else {
                    next.prev = next;
                }
                size--;
            }

            // 删除链表中最后一个节点，并返回该节点，时间 O(1)
            public Node removeLast() {
                if (null == tail) {
                    return null;
                }
                Node tem = tail;
                Node prev = tail.prev;
                prev.next = null;
                tail = prev;
                size--;
                return tem;
            }

            public Node getLast() {
                return tail;
            }

            // 返回链表长度，时间 O(1)
            public int getSize() {
                return size;
            }
        }

        class Node {
            private final K key;
            private final V data;
            public Node next, prev;
            private final long lastModified;

            private Node(K key, V data, long lastModified) {
                this.key = key;
                this.data = data;
                this.lastModified = lastModified;
            }
            private boolean hasExpire(Long lastModified){
                return this.lastModified < lastModified;
            }

            private boolean hasExpire(){
                return System.currentTimeMillis() - this.lastModified > expireAfterWrite;
            }
        }
    }

    private class ExpireableWithLRUCache extends ExpireableCache{

        private final int initialCapacity;

        public ExpireableWithLRUCache(int initialCapacity, long expireAfterWrite) {
            super(initialCapacity, expireAfterWrite);
            this.initialCapacity = initialCapacity;
        }

        @Override
        public V get(K key) {
            Node node = super.cacheTabe.get(key);
            if (null == node) {
                return null;
            }
            writeLock.lock();
            try {
                cacheDoubleList.remove(node);
                node = cacheDoubleList.addFirst(node);
                cacheTabe.put(node.key, node);
                return node.data;
            } finally {
                writeLock.unlock();
            }
        }

        /**
         * 覆盖式写缓存，如果存在相应key的缓存会覆盖原来的值且延长过期时间
         *
         * @param key
         * @param data
         */
        @Override
        public void put(K key, V data) {
            innerSyncLRUCheck(key);
            super.put(key, data);
        }

        private void innerSyncLRUCheck(K key){
            writeLock.lock();
            try {
                if (cacheTabe.containsKey(key)) {
                    Node originNode = cacheTabe.get(key);
                    cacheDoubleList.remove(originNode);
                    cacheTabe.remove(key);
                } else {
                    int size = super.getSize();
                    if (this.initialCapacity == size) {
                        // 容量已满
                        // 淘汰最近未使用的
                        Node last = cacheDoubleList.removeLast();
                        cacheTabe.remove(last.key);
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }
    }
}
