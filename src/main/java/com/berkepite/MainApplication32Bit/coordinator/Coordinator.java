package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.subscribers.*;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class Coordinator implements CommandLineRunner {

    private final Logger LOGGER = LogManager.getLogger(Coordinator.class);

    private final Properties properties;
    private final SubscriberMapper subscriberMapper;
    private final SubscriberLoader subscriberLoader;
    private List<ISubscriber> subscribers;

    @Autowired
    public Coordinator(@Qualifier("subscribersProperties") Properties properties, SubscriberMapper subscriberMapper, SubscriberLoader subscriberLoader) {
        this.properties = properties;
        this.subscriberMapper = subscriberMapper;
        this.subscriberLoader = subscriberLoader;
    }

    @PostConstruct
    private void init() {
        subscribers = new ArrayList<>();
        bindSubscribers();
        LOGGER.debug("Coordinator Initialized!");
    }

    @Override
    public void run(String... args) throws Exception {
    }

    private void bindSubscribers() {
        List<SubscriberBindingConfig> subscriberBindingConfigs = subscriberMapper.mapSubscriberBindingConfigs(properties);
        loadSubscriberClasses(subscriberBindingConfigs);
    }

    private void loadSubscriberClasses(List<SubscriberBindingConfig> subscriberBindingConfigs) {
        subscriberBindingConfigs.forEach(subscriberBindingConfig -> {
            if (subscriberBindingConfig.isEnabled())
                subscribers.add(subscriberLoader.load(subscriberBindingConfig));
        });
    }

    public List<ISubscriber> getSubscribers() {
        return subscribers;
    }
}
