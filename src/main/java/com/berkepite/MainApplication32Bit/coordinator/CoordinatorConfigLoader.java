package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.ConfigMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class CoordinatorConfigLoader {
    @Value("${coordinator.config-name}")
    private String configName;
    private final ConfigMapper configMapper;
    private final Logger LOGGER = LogManager.getLogger(CoordinatorConfigLoader.class);

    public CoordinatorConfigLoader(ConfigMapper configMapper) {
        this.configMapper = configMapper;
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
            coordinatorConfig.setSubscriberBindingConfigs(configMapper.mapSubscriberBindingConfigs(properties));
            coordinatorConfig.setRates(configMapper.mapCoordinatorRates(properties));
        } catch (NullPointerException e) {
            LOGGER.error("Could not map coordinator configs from {}", configName, e);
            throw new RuntimeException();
        }

        return coordinatorConfig;
    }
}
