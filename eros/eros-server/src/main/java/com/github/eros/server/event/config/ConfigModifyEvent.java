package com.github.eros.server.event.config;

import com.github.eros.client.retrofit.entry.ConfigInfo;
import com.github.eros.server.event.Event;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:55
 */
public class ConfigModifyEvent extends Event {

    public ConfigModifyEvent(ConfigInfo source) {
        super(source);
    }

    public ConfigInfo getConfigInfo() {
        return (ConfigInfo)source;
    }
}
