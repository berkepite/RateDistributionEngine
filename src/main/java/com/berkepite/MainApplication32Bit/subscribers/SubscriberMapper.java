package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.rates.RateEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
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
        SubscriberConfig config = new SubscriberConfig();

        config.setName(properties.getProperty("name"));
        config.setClassPath(properties.getProperty("classPath"));
        config.setClassName(properties.getProperty("className"));
        config.setUrl(properties.getProperty("url"));
        config.setUsername(properties.getProperty("username"));
        config.setPassword(properties.getProperty("password"));

        if (properties.getProperty("includeRates") != null && !properties.getProperty("includeRates").isEmpty()) {
            List<RateEnum> includeRates = new ArrayList<>();
            Arrays.stream(properties.getProperty("includeRates").split(",")).forEach(rate -> includeRates.add(RateEnum.valueOf(rate)));
            config.setIncludeRates(includeRates);
        }
        if (properties.getProperty("excludeRates") != null && !properties.getProperty("excludeRates").isEmpty()) {
            List<RateEnum> excludeRates = new ArrayList<>();
            Arrays.stream(properties.getProperty("excludeRates").split(",")).forEach(rate -> excludeRates.add(RateEnum.valueOf(rate)));
            config.setExcludeRates(excludeRates);
        }

        LOGGER.info("Mapped subscriber config: {}", config);

        return config;
    }
}
