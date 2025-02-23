package com.berkepite.MainApplication32Bit.coordinator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class CoordinatorConfigLoader {
    @Value("${app.coordinator-config-name}")
    private String configName;
    private final CoordinatorConfigMapper coordinatorConfigMapper;
    private final Logger LOGGER = LogManager.getLogger(CoordinatorConfigLoader.class);

    public CoordinatorConfigLoader(CoordinatorConfigMapper coordinatorConfigMapper) {
        this.coordinatorConfigMapper = coordinatorConfigMapper;
    }

    private Properties loadProperties() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource(configName));

        Properties properties = yaml.getObject();

        if (properties == null) {
            LOGGER.error("Could not load properties from {}", configName);
            throw new RuntimeException();
        }

        return properties;
    }

    public CoordinatorConfig loadConfig() {
        Properties properties = loadProperties();
        CoordinatorConfig coordinatorConfig = new CoordinatorConfig();

        try {
            coordinatorConfig.setSubscriberBindingConfigs(coordinatorConfigMapper.mapSubscriberBindingConfigs(properties));
            coordinatorConfig.setRates(coordinatorConfigMapper.mapCoordinatorRates(properties));
        } catch (NullPointerException e) {
            LOGGER.error("Could not map coordinator configs from {}", configName, e);
            throw new RuntimeException();
        }

        return coordinatorConfig;
    }
}
