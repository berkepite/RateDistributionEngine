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

/**
 * Coordinator class responsible for managing subscribers and processing rates.
 * It implements {@link CommandLineRunner} and {@link ICoordinator} interfaces to manage application startup and subscriber events.
 */
@Component
public class Coordinator implements CommandLineRunner, ICoordinator {

    private final Logger LOGGER = LogManager.getLogger(Coordinator.class);

    private CoordinatorConfig coordinatorConfig;
    private final RateService rateService;
    private final SubscriberLoader subscriberLoader;
    private final CoordinatorConfigLoader coordinatorConfigLoader;
    private final ThreadPoolTaskExecutor executorService;

    private List<ISubscriber> subscribers;

    /**
     * Constructor for initializing the Coordinator with necessary services.
     *
     * @param rateService             the service for managing rates
     * @param coordinatorConfigLoader the loader for coordinator configuration
     * @param subscriberLoader        the loader for subscribers
     * @param executorService         the thread pool executor for managing async tasks
     */
    @Autowired
    public Coordinator(RateService rateService, CoordinatorConfigLoader coordinatorConfigLoader, SubscriberLoader subscriberLoader, @Qualifier("coordinatorExecutor") ThreadPoolTaskExecutor executorService) {
        this.coordinatorConfigLoader = coordinatorConfigLoader;
        this.subscriberLoader = subscriberLoader;
        this.executorService = executorService;
        this.rateService = rateService;
    }

    /**
     * Initializes the coordinator by loading configuration and setting up shutdown hooks.
     * This method is called after the constructor.
     */
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

    /**
     * Runs the coordinator by binding subscribers and initiating connections.
     *
     * @param args command line arguments passed to the application
     */
    @Override
    public void run(String... args) {
        bindSubscribers();

        for (ISubscriber subscriber : subscribers) {
            executorService.execute(subscriber::connect);
        }
    }

    /**
     * Binds the subscribers to the coordinator.
     */
    private void bindSubscribers() {
        loadSubscriberClasses(coordinatorConfig.getSubscriberBindingConfigs());
    }

    /**
     * Loads subscriber classes based on the provided configurations.
     *
     * @param subscriberBindingConfigs list of subscriber binding configurations
     */
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

    /**
     * Returns the list of subscribers.
     *
     * @return the list of subscribers
     */
    public List<ISubscriber> getSubscribers() {
        return subscribers;
    }

    /**
     * Returns the coordinator configuration.
     *
     * @return the coordinator configuration
     */
    public CoordinatorConfig getCoordinatorConfig() {
        return coordinatorConfig;
    }

    /**
     * Handles the connection event for a subscriber.
     *
     * @param subscriber the subscriber that has connected
     */
    @Override
    public void onConnect(ISubscriber subscriber) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} connected to {}, trying to subscribe...", config.getName(), config.getUrl());
        executorService.execute(() -> subscriber.subscribe(coordinatorConfig.getRates()));
    }

    /**
     * Handles the subscription event for a subscriber.
     *
     * @param subscriber the subscriber that has subscribed
     */
    @Override
    public void onSubscribe(ISubscriber subscriber) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} subscribed to {}", config.getName(), config.getUrl());
    }

    /**
     * Handles the unsubscription event for a subscriber.
     *
     * @param subscriber the subscriber that has unsubscribed
     */
    @Override
    public void onUnSubscribe(ISubscriber subscriber) {
        // No action required in this implementation
    }

    /**
     * Handles the disconnection event for a subscriber.
     *
     * @param subscriber the subscriber that has disconnected
     */
    @Override
    public void onDisConnect(ISubscriber subscriber) {
        LOGGER.info("{} stopped listening/requesting.", subscriber.getConfig().getName());
    }

    /**
     * Handles the event when a rate is available from a subscriber.
     *
     * @param subscriber the subscriber providing the rate
     * @param rate       the available rate
     */
    @Override
    public void onRateAvailable(ISubscriber subscriber, RawRateEnum rate) {
        LOGGER.info("({}) rate available {}", subscriber.getConfig().getName(), rate);
    }

    /**
     * Handles the event when a rate update is received from a subscriber.
     *
     * @param subscriber the subscriber providing the rate update
     * @param rate       the rate
     */
    @Override
    public void onRateUpdate(ISubscriber subscriber, RawRate rate) {
        LOGGER.info("({}) rate received {}", subscriber.getConfig().getName(), rate.toString());

        executorService.execute(() -> rateService.manageRawRate(rate));
    }

    /**
     * Handles the event when a rate error occurs for a subscriber.
     *
     * @param subscriber the subscriber experiencing the error
     * @param status     the rate error status
     */
    @Override
    @CoordinatorEventStatus
    public void onRateError(ISubscriber subscriber, RateStatus status) {
        // No action required in this implementation
    }

    /**
     * Handles the event when a connection error occurs for a subscriber.
     *
     * @param subscriber the subscriber experiencing the connection error
     * @param status     the connection error status
     */
    @Override
    @CoordinatorEventStatus
    public void onConnectionError(ISubscriber subscriber, ConnectionStatus status) {
        // No action required in this implementation
    }
}
