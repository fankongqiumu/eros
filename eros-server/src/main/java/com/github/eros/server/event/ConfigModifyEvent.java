package com.github.eros.server.event;

import com.github.eros.domain.Config;
import org.springframework.context.ApplicationEvent;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:55
 */
public class ConfigModifyEvent extends ApplicationEvent {
    
    private String namespace;
    
    public ConfigModifyEvent(String namespace) {
        super(namespace);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
