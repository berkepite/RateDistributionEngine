package com.berkepite.MainApplication32Bit.subscribers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class SubscriberMapper {
    private final Logger LOGGER = LogManager.getLogger(SubscriberMapper.class);

    public List<SubscriberBindingConfig> mapSubscriberBindingConfigs(Properties properties) {
        List<SubscriberBindingConfig> subscriberBindingConfigs = new ArrayList<>();
        int index = 0;

        while (properties.containsKey("coordinator.subscribers[" + index + "].name")) {
            SubscriberBindingConfig model = new SubscriberBindingConfig();

            model.setName(properties.getProperty("coordinator.subscribers[" + index + "].name"));
            model.setEnabled(Boolean.parseBoolean(properties.getProperty("coordinator.subscribers[" + index + "].enabled")));
            model.setConfigName(properties.getProperty("coordinator.subscribers[" + index + "].configName"));

            subscriberBindingConfigs.add(model);
            index++;
        }

        LOGGER.debug("Mapped subscriber config models: {}", subscriberBindingConfigs);

        return subscriberBindingConfigs;
    }

    public SubscriberConfig mapSubscriberConfig(Properties properties) {
        SubscriberConfig model = new SubscriberConfig();

        model.setName(properties.getProperty("name"));
        model.setClassPath(properties.getProperty("classPath"));
        model.setClassName(properties.getProperty("className"));
        model.setUrl(properties.getProperty("url"));
        model.setPort(properties.getProperty("port"));
        model.setUsername(properties.getProperty("username"));
        model.setPassword(properties.getProperty("password"));

        LOGGER.debug("Mapped subscriber model: {}", model);

        return model;
    }
}
