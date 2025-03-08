package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.RawRate;
import com.berkepite.MainApplication32Bit.rates.RateService;
import com.berkepite.MainApplication32Bit.status.ConnectionStatus;
import com.berkepite.MainApplication32Bit.rates.RawRateEnum;
import com.berkepite.MainApplication32Bit.status.RateStatus;
import com.berkepite.MainApplication32Bit.subscribers.*;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Coordinator implements CommandLineRunner, ICoordinator {

    private final Logger LOGGER = LogManager.getLogger(Coordinator.class);

    private CoordinatorConfig coordinatorConfig;
    private final RateService rateService;
    private final SubscriberLoader subscriberLoader;
    private final CoordinatorConfigLoader coordinatorConfigLoader;
    private final ThreadPoolTaskExecutor executorService;

    private List<ISubscriber> subscribers;

    @Autowired
    public Coordinator(RateService rateService, CoordinatorConfigLoader coordinatorConfigLoader, SubscriberLoader subscriberLoader, @Qualifier("coordinatorExecutor") ThreadPoolTaskExecutor executorService) {
        this.coordinatorConfigLoader = coordinatorConfigLoader;
        this.subscriberLoader = subscriberLoader;
        this.executorService = executorService;
        this.rateService = rateService;
    }

    @PostConstruct
    private void init() {
        coordinatorConfig = coordinatorConfigLoader.loadConfig();
        subscribers = new ArrayList<>(3);

        LOGGER.debug("Coordinator Initialized!");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();

            LOGGER.info("Executor stopped. ({})", this.getClass().getSimpleName());
        }, "shutdown-hook-coordinator"));

    }

    @Override
    public void run(String... args) {
        bindSubscribers();

        for (ISubscriber subscriber : subscribers) {
            executorService.execute(subscriber::connect);
        }

    }

    private void bindSubscribers() {
        loadSubscriberClasses(coordinatorConfig.getSubscriberBindingConfigs());
    }

    private void loadSubscriberClasses(List<SubscriberBindingConfig> subscriberBindingConfigs) {
        subscriberBindingConfigs.forEach(subscriberBindingConfig -> {
            if (subscriberBindingConfig.isEnabled()) {
                ISubscriber subscriber = subscriberLoader.load(subscriberBindingConfig);
                if (subscriber != null) {
                    subscribers.add(subscriber);
                }
            }
        });
    }

    public List<ISubscriber> getSubscribers() {
        return subscribers;
    }

    public CoordinatorConfig getCoordinatorConfig() {
        return coordinatorConfig;
    }

    @Override
    public void onConnect(ISubscriber subscriber) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} connected to {}, trying to subscribe...", config.getName(), config.getUrl());
        executorService.execute(() -> subscriber.subscribe(coordinatorConfig.getRates()));
    }

    @Override
    public void onSubscribe(ISubscriber subscriber) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} subscribed to {}", config.getName(), config.getUrl());
    }

    @Override
    public void onUnSubscribe(ISubscriber subscriber) {

    }

    @Override
    public void onDisConnect(ISubscriber subscriber) {
        LOGGER.info("{} stopped listening/requesting.", subscriber.getConfig().getName());
    }

    @Override
    public void onRateAvailable(ISubscriber subscriber, RawRateEnum rate) {
        LOGGER.info("({}) rate available {}", subscriber.getConfig().getName(), rate);
    }

    @Override
    public void onRateUpdate(ISubscriber subscriber, RawRate rate) {
        LOGGER.info("({}) rate received {}", subscriber.getConfig().getName(), rate.toString());

        executorService.execute(() -> {
            rateService.manageRawRate(rate);
        });
    }

    @Override
    @CoordinatorEventStatus
    public void onRateError(ISubscriber subscriber, RateStatus status) {

    }

    @Override
    @CoordinatorEventStatus
    public void onConnectionError(ISubscriber subscriber, ConnectionStatus status) {

    }

}
