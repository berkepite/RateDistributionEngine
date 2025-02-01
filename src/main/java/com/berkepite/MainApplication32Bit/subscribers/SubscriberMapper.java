package com.berkepite.MainApplication32Bit.subscribers;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class SubscriberMapper {

    public List<SubscriberModel> map(Properties properties) {
        List<SubscriberModel> subscribers = new ArrayList<>();
        int index = 0;

        while (properties.containsKey("coordinator.subscribers[" + index + "].name")) {
            SubscriberModel model = new SubscriberModel();

            model.setName(properties.getProperty("coordinator.subscribers[" + index + "].name"));
            model.setEnabled(Boolean.parseBoolean(properties.getProperty("coordinator.subscribers[" + index + "].enabled")));
            model.setConfigPath(properties.getProperty("coordinator.subscribers[" + index + "].configPath"));

            subscribers.add(model);
            index++;
        }

        return subscribers;
    }
}
