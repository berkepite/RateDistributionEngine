package com.berkepite.MainApplication32Bit;

import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.subscribers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Component
public class ConfigMapper {
    private final Logger LOGGER = LogManager.getLogger(ConfigMapper.class);

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

            subscriberBindingConfigs.add(model);
            index++;
        }

        LOGGER.debug("Mapped subscriber config models: {}", subscriberBindingConfigs);

        return subscriberBindingConfigs;
    }

    public ISubscriberConfig mapSubscriberConfig(Properties properties) {
        ISubscriberConfig subscriberConfig;

        if (properties.getProperty("name").equals("bloomberg_rest")) {

            subscriberConfig = new BloombergRestConfig();
            BloombergRestConfig temp = (BloombergRestConfig) subscriberConfig;
            temp.setRequestInterval(properties.getProperty("requestInterval"));
            temp.setRequestRetryLimit(properties.getProperty("requestRetryLimit"));

        } else if (properties.getProperty("name").equals("cnn_tcp")) {

            subscriberConfig = new CNNTCPConfig();
            CNNTCPConfig temp = (CNNTCPConfig) subscriberConfig;
            temp.setRequestInterval(properties.getProperty("requestInterval"));
            temp.setRequestRetryLimit(properties.getProperty("requestRetryLimit"));

        } else {
            LOGGER.error("COULD NOT MAP subscriber config: {}", properties.getProperty("name"));
            return null;
        }

        subscriberConfig.setName(properties.getProperty("name"));
        subscriberConfig.setClassPath(properties.getProperty("classPath"));
        subscriberConfig.setClassName(properties.getProperty("className"));
        subscriberConfig.setUrl(properties.getProperty("url"));
        subscriberConfig.setPort(Integer.parseInt(properties.getProperty("port")));
        subscriberConfig.setUsername(properties.getProperty("username"));
        subscriberConfig.setPassword(properties.getProperty("password"));

        if (properties.getProperty("includeRates") != null && !properties.getProperty("includeRates").isEmpty()) {
            List<RateEnum> includeRates = new ArrayList<>();
            Arrays.stream(properties.getProperty("includeRates").split(",")).forEach(rate -> includeRates.add(RateEnum.valueOf(rate)));
            subscriberConfig.setIncludeRates(includeRates);
        }
        if (properties.getProperty("excludeRates") != null && !properties.getProperty("excludeRates").isEmpty()) {
            List<RateEnum> excludeRates = new ArrayList<>();
            Arrays.stream(properties.getProperty("excludeRates").split(",")).forEach(rate -> excludeRates.add(RateEnum.valueOf(rate)));
            subscriberConfig.setExcludeRates(excludeRates);
        }

        LOGGER.debug("Mapped subscriber config: {}", subscriberConfig);

        return subscriberConfig;
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
}
