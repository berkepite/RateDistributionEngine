package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.RateEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "coordinator")
public class CoordinatorConfigLoader {
    @Value("${coordinator.config-name}")
    private String configName;

    public Properties getProperties() {
        try {
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResources(new ClassPathResource(configName));

            return yaml.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CoordinatorConfig loadConfig(Properties properties) {
        CoordinatorConfig coordinatorConfig = new CoordinatorConfig();
        properties.keys().asIterator().forEachRemaining(key -> {
            if (key.toString().contains("coordinator.rates"))
                coordinatorConfig.addRate(RateEnum.valueOf(key.toString().substring("coordinator.rates".length() + 1)));
        });

        return coordinatorConfig;
    }
}
