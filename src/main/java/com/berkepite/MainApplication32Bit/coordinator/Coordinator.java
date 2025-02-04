package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.ConfigMapper;
import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.rates.RateStatus;
import com.berkepite.MainApplication32Bit.subscribers.*;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Coordinator implements CommandLineRunner, ICoordinator {

    private final Logger LOGGER = LogManager.getLogger(Coordinator.class);

    private CoordinatorConfig coordinatorConfig;
    private final ConfigMapper configMapper;
    private final SubscriberLoader subscriberLoader;
    private final CoordinatorConfigLoader coordinatorConfigLoader;
    private List<ISubscriber> subscribers;

    @Autowired
    public Coordinator(CoordinatorConfigLoader coordinatorConfigLoader, ConfigMapper configMapper, SubscriberLoader subscriberLoader) {
        this.coordinatorConfigLoader = coordinatorConfigLoader;
        this.configMapper = configMapper;
        this.subscriberLoader = subscriberLoader;
    }

    @PostConstruct
    private void init() {
        coordinatorConfig = coordinatorConfigLoader.loadConfig();
        subscribers = new ArrayList<>();

        bindSubscribers();

        LOGGER.trace("Coordinator Initialized!");
    }

    @Override
    public void run(String... args) throws Exception {


    }

    private void bindSubscribers() {
        loadSubscriberClasses(coordinatorConfig.getSubscriberBindingConfigs());
    }

    private void loadSubscriberClasses(List<SubscriberBindingConfig> subscriberBindingConfigs) {
        subscriberBindingConfigs.forEach(subscriberBindingConfig -> {
            if (subscriberBindingConfig.isEnabled())
                subscribers.add(subscriberLoader.load(subscriberBindingConfig, this));
        });
    }

    public List<ISubscriber> getSubscribers() {
        return subscribers;
    }

    public CoordinatorConfig getCoordinatorConfig() {
        return coordinatorConfig;
    }

    public void setCoordinatorConfig(CoordinatorConfig coordinatorConfig) {
        this.coordinatorConfig = coordinatorConfig;
    }

    @Override
    public void onConnect(SubscriberConfig subscriberConfig, Boolean status) {

    }

    @Override
    public void onDisConnect(SubscriberConfig subscriberConfig) {

    }

    @Override
    public void onRateAvailable(SubscriberConfig subscriberConfig, RateEnum rate) {

    }

    @Override
    public void onRateUpdate(SubscriberConfig subscriberConfig, IRate rate) {

    }

    @Override
    public void onRateStatus(SubscriberConfig subscriberConfig, RateStatus rateStatus) {

    }

}
