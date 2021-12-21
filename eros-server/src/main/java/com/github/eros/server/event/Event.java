package com.github.eros.server.event;

import java.io.Serializable;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:36
 */
public abstract class Event implements Serializable {
    private static final long serialVersionUID = -312990870859527840L;

    protected transient Object  source;

    private final long timestamp;


    public Event(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("null source");
        }
        this.timestamp = System.currentTimeMillis();
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

    public final long getTimestamp() {
        return this.timestamp;
    }


    @Override
    public String toString() {
        return getClass().getName() + "[source=" + source + "timestamp=" + timestamp + "]";
    }


}
