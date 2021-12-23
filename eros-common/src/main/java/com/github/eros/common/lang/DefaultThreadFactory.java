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
    private Boolean isDaemon;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public static ThreadFactory defaultThreadFactory(String namePrefix) {
        Objects.requireNonNull(namePrefix);
        return new DefaultThreadFactory(namePrefix);
    }

    public static ThreadFactory defaultThreadFactory(String namePrefix, Boolean isDaemon) {
        Objects.requireNonNull(namePrefix);
        return new DefaultThreadFactory(namePrefix, isDaemon);
    }


    DefaultThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    DefaultThreadFactory(String namePrefix, Boolean isDaemon) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
        this.isDaemon = isDaemon;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(group, runnable,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (null != this.isDaemon) {
            thread.setDaemon(this.isDaemon);
        } else {
            thread.setDaemon(true);
        }
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
}