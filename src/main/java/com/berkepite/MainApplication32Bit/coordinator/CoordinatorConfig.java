package com.berkepite.MainApplication32Bit.coordinator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

@Configuration
public class CoordinatorConfig {

    @Value("${coordinator.config-name}")
    private String configName;

    @Bean
    public Properties subscribersProperties() {
        try {
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResources(new ClassPathResource(configName));
            return yaml.getObject();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }
}