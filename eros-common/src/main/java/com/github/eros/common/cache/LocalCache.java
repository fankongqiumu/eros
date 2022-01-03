package com.github.eros.common.cache;

import com.github.eros.common.lang.DefaultThreadFactory;
import com.github.eros.common.lang.DoubleList;
import com.github.eros.common.lang.Node;

import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalCache<K, V> {

    private final CacheEngine<K, V> cacheEngine;

    protected LocalCache(int initialCapacity, boolean sync, boolean lru, long expireAfterWrite, ExpireCallBack<K, V>...expireCallBacks) {
        if (lru){
            this.cacheEngine = new ExpireableWithLRUCache(initialCapacity, Long.MIN_VALUE, expireCallBacks);
        } else if (sync && expireAfterWrite > 0L){
            this.cacheEngine = new ExpireableCache(initialCapacity, expireAfterWrite, expireCallBacks);
        } else if (sync) {
            this.cacheEngine = new SimpleSynCache(initialCapacity);
        } else {
            this.cacheEngine = new SimpleCache(initialCapacity);
        }
    }

    public V get(K key) {
        return this.cacheEngine.get(key);
    }

    public boolean containsKey(K key) {
        return this.cacheEngine.containsKey(key);
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

    public static <K, V> LocalCache<K, V> buildSimpleCache(int initialCapacity){
        return new LocalCache<>(initialCapacity, false, false, Long.MIN_VALUE);
    }

    public static <K, V> LocalCache<K, V> buildSimpleSynCache(int initialCapacity){
        return new LocalCache<>(initialCapacity, true, false, Long.MIN_VALUE);
    }

    public static <K, V> LocalCache<K, V> buildExpireableCache(int initialCapacity, long expireAfterWrite) {
        return new LocalCache<>(initialCapacity, true, false, expireAfterWrite);
    }

    public static <K, V> LocalCache<K, V> buildLRUCache(int initialCapacity) {
        return new LocalCache<>(initialCapacity, true, true, Long.MIN_VALUE);
    }

    public static <K, V> LocalCache<K, V> buildLRUCacheWithExpireCallBack(int initialCapacity, ExpireCallBack expireCallBack) {
        return new LocalCache<>(initialCapacity, true, true, Long.MIN_VALUE);
    }

    protected class SimpleCache implements CacheEngine<K, V>{

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
        public boolean containsKey(K key) {
            return cacheTabe.containsKey(key);
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

    protected class SimpleSynCache extends SimpleCache{
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

    protected class ExpireableCache implements CacheEngine<K, V>, Runnable{
        protected final LinkedHashMap<K, Node<K, V>> cacheTabe;
        protected final DoubleList<K, V> cacheDoubleList;
        protected final ExpireCallBack<K, V>[] expireCallBacks;
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
        public ExpireableCache(int initialCapacity, long expireAfterWrite, ExpireCallBack<K, V>... expireCallBacks) {
            this.cacheTabe = new LinkedHashMap<>(initialCapacity);
            this.cacheDoubleList = new DoubleList<>();
            this.expireAfterWrite = expireAfterWrite;
            this.expireCallBacks = expireCallBacks;
            if (expireAfterWrite <= 0L) {
                scheduledExecutorService = null;
            } else {
                scheduledExecutorService = new ScheduledThreadPoolExecutor(2,
                        DefaultThreadFactory.defaultThreadFactory("localCache-expire-thread")
                );
                scheduledExecutorService.scheduleAtFixedRate(this, 1, 10, TimeUnit.SECONDS);
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    private volatile boolean hasShutdown = false;
                    @Override
                    public void run() {
                        synchronized (ExpireableCache.class) {
                            if (!this.hasShutdown) {
                                this.hasShutdown = true;
                                long beginTime = System.currentTimeMillis();
                                if (!scheduledExecutorService.isShutdown()){
                                    scheduledExecutorService.shutdown();
                                }
                                long consumingTimeTotal = System.currentTimeMillis() - beginTime;
                                System.out.println("Shutdown hook over, consuming total time(ms):" + consumingTimeTotal);
                            }
                        }
                    }
                }, "ShutdownHook"));
            }
        }

        @Override
        public V get(K key) {
            Node<K, V> node = cacheTabe.get(key);
            if (null != node) {
                if (node.hasExpire(expireAfterWrite)) {
                    expire(key);
                    return null;
                }
                return node.getData();
            }
            return null;
        }

        @Override
        public Long getlastModified(K key){
            Node<K, V> node = cacheTabe.get(key);
            if (null != node) {
                if (node.hasExpire(expireAfterWrite)) {
                    expire(key);
                    return null;
                }
                return node.getLastModified();
            }
            return null;
        }

        @Override
        public boolean containsKey(K key) {
            return cacheTabe.containsKey(key);
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
                Node<K, V> node = cacheTabe.get(key);
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
            return cacheDoubleList.getSize();
        }

        private void innerSyncCache(K key, V data, Long lastModified) {
            if (cacheTabe.containsKey(key)) {
                innerSyncExpire(key);
            }
            writeLock.lock();
            try {
                Node<K, V> node = new Node<>(key, data, lastModified);
                node = cacheDoubleList.addFirst(node);
                cacheTabe.put(node.getKey(), node);
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
                Node<K, V> originNode = cacheTabe.get(key);
                if (null == originNode){
                    return;
                }
                cacheTabe.remove(key);
                cacheDoubleList.remove(originNode);
                if (null != expireCallBacks && expireCallBacks.length > 0){
                    for (ExpireCallBack<K, V> expireCallBack : expireCallBacks) {
                        expireCallBack.callBack(originNode.getKey(), originNode.getData());
                    }
                }
                System.out.println("key：" + key + "data:" + originNode.getData() + "expire...");
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
                Node<K, V> last = cacheDoubleList.getLast();
                while (null != last) {
                    if (System.currentTimeMillis() - last.getLastModified() > this.expireAfterWrite) {
                        innerSyncExpire(last.getKey());
                        last = cacheDoubleList.getLast();
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }
    }

    protected class ExpireableWithLRUCache extends ExpireableCache{

        private final int initialCapacity;
        public ExpireableWithLRUCache(int initialCapacity, long expireAfterWrite, ExpireCallBack<K, V>...expireCallBacks) {
            super(initialCapacity, expireAfterWrite, expireCallBacks);
            this.initialCapacity = initialCapacity;
        }

        @Override
        public V get(K key) {
            Node<K, V> node = super.cacheTabe.get(key);
            if (null == node) {
                return null;
            }
            writeLock.lock();
            try {
                cacheDoubleList.remove(node);
                node = cacheDoubleList.addFirst(node);
                cacheTabe.put(node.getKey(), node);
                return node.getData();
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
                    Node<K, V> originNode = cacheTabe.get(key);
                    cacheDoubleList.remove(originNode);
                    cacheTabe.remove(key);
                } else {
                    int size = super.getSize();
                    if (this.initialCapacity == size) {
                        // 容量已满
                        // 淘汰最近未使用的
                        Node<K, V> last = cacheDoubleList.removeLast();
                        cacheTabe.remove(last.getKey());
                        if (null != expireCallBacks && expireCallBacks.length > 0){
                            for (ExpireCallBack<K, V> expireCallBack : expireCallBacks) {
                                expireCallBack.callBack(last.getKey(), last.getData());
                            }
                        }
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }
    }
}
