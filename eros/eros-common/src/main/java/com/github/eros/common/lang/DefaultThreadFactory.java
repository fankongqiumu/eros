package com.github.eros.common.lang;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 00:38
 */
public class DefaultThreadFactory  implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public static ThreadFactory defaultThreadFactory(String namePrefix) {
        Objects.requireNonNull(namePrefix);
        return new DefaultThreadFactory(namePrefix);
    }


    DefaultThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
