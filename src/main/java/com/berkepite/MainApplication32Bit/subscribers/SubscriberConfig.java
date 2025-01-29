package com.berkepite.MainApplication32Bit.subscribers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriberConfig {

    private final SubscriberConfigLoader configLoader;

    @Autowired
    public SubscriberConfig(SubscriberConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

}
