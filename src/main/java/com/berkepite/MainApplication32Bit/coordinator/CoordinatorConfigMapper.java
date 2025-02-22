package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.subscribers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class CoordinatorConfigMapper {
    private final Logger LOGGER = LogManager.getLogger(CoordinatorConfigMapper.class);

    public List<SubscriberBindingConfig> mapSubscriberBindingConfigs(Properties properties) throws NullPointerException {
        if (properties == null)
            throw new NullPointerException();


        List<SubscriberBindingConfig> subscriberBindingConfigs = new ArrayList<>();
        int index = 0;

        while (properties.containsKey("coordinator.subscribers[" + index + "].name")) {
            SubscriberBindingConfig model = new SubscriberBindingConfig();

            model.setName(properties.getProperty("coordinator.subscribers[" + index + "].name"));
            model.setEnabled(Boolean.parseBoolean(properties.getProperty("coordinator.subscribers[" + index + "].enabled")));
            model.setConfigName(properties.getProperty("coordinator.subscribers[" + index + "].configName"));
            model.setClassName(properties.getProperty("coordinator.subscribers[" + index + "].className"));
            model.setClassPath(properties.getProperty("coordinator.subscribers[" + index + "].classPath"));

            subscriberBindingConfigs.add(model);
            index++;
        }

        LOGGER.debug("Mapped subscriber config models: {}", subscriberBindingConfigs);

        return subscriberBindingConfigs;
    }

    public List<RateEnum> mapCoordinatorRates(Properties properties) throws NullPointerException {
        if (properties == null)
            throw new NullPointerException();

        List<RateEnum> rates = new ArrayList<>();

        properties.keys().asIterator().forEachRemaining(key -> {
            if (key.toString().contains("coordinator.rates"))
                rates.add(RateEnum.valueOf(key.toString().substring("coordinator.rates".length() + 1)));
        });

        LOGGER.debug("Mapped rates: {}", rates);

        return rates;
    }

    public String mapCoordinatorRateCalculationStrategy(Properties properties) throws NullPointerException {
        if (properties == null)
            throw new NullPointerException();

        String strategy = properties.getProperty("coordinator.rateCalculationStrategy");

        LOGGER.debug("Mapped rate calculation strategy: {}", strategy);
        return strategy;
    }

    public String mapCoordinatorRateCalculationSourcePath(Properties properties) throws NullPointerException {
        if (properties == null)
            throw new NullPointerException();

        String path = properties.getProperty("coordinator.rateCalculationSourcePath");

        LOGGER.debug("Mapped rate calculation source path: {}", path);
        return path;
    }
}
