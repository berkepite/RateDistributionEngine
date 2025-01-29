package com.berkepite.MainApplication32Bit.coordinator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoordinatorConfig {
    private final CoordinatorProperties properties;

    @Autowired
    public CoordinatorConfig(CoordinatorPropertiesLoader propertiesLoader) {
        this.properties = propertiesLoader.getCoordinatorProperties();
    }

    public CoordinatorProperties getProperties() {
        return properties;
    }
}
