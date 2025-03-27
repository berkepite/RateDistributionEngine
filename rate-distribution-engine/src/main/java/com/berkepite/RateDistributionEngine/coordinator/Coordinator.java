package com.berkepite.RateDistributionEngine.coordinator;

import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.rates.RateManager;
import com.berkepite.RateDistributionEngine.common.status.ConnectionStatus;
import com.berkepite.RateDistributionEngine.common.status.RateStatus;
import com.berkepite.RateDistributionEngine.common.ISubscriber;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.subscribers.SubscriberLoader;
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

    private final CoordinatorConfig coordinatorConfig;
    //private final RateService rateService;
    private final SubscriberLoader subscriberLoader;
    private final ThreadPoolTaskExecutor executorService;

    private List<ISubscriber> subscribers;

    /**
     * Constructor for initializing the Coordinator with necessary services.
     *
     * @param coordinatorConfig the configuration class for the coordinator
     * @param rateManager       the service for managing rates
     * @param subscriberLoader  the loader for subscribers
     * @param executorService   the thread pool executor for managing async tasks
     */
    @Autowired
    public Coordinator(CoordinatorConfig coordinatorConfig, RateManager rateManager, SubscriberLoader subscriberLoader, @Qualifier("coordinatorExecutor") ThreadPoolTaskExecutor executorService) {
        this.coordinatorConfig = coordinatorConfig;
        this.subscriberLoader = subscriberLoader;
        this.executorService = executorService;
        //this.rateService = rateService;
    }

    /**
     * Initializes the coordinator by setting up shutdown hooks.
     * Initializes the subscriber list
     * This method is called after the constructor.
     */
    @PostConstruct
    private void init() {
        subscribers = new ArrayList<>(2);
        loadSubscriberClasses(coordinatorConfig.getSubscribers());

        LOGGER.info("Subscriber classes loaded!: {}", subscribers);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();

            LOGGER.info("Executor stopped. ({})", this.getClass().getSimpleName());
        }, "shutdown-hook-coordinator"));

        LOGGER.debug("Coordinator Initialized!");
    }

    /**
     * Runs the coordinator by binding subscribers and initiating connections.
     *
     * @param args command line arguments passed to the application
     */
    @Override
    public void run(String... args) {
        for (ISubscriber subscriber : subscribers) {
            executorService.execute(subscriber::connect);
        }
    }

    /**
     * Loads subscriber classes based on the provided configurations.
     *
     * @param subscriberBindingConfigs list of subscriber binding configurations
     */
    private void loadSubscriberClasses(List<CoordinatorConfig.SubscriberBindingConfig> subscriberBindingConfigs) {
        subscriberBindingConfigs.forEach(subscriberBindingConfig -> {
            if (subscriberBindingConfig.isEnabled()) {
                ISubscriber subscriber = subscriberLoader.load(subscriberBindingConfig, this);
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
        LOGGER.info("Subscriber rates: {}", coordinatorConfig.getRates());

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
    public void onRateAvailable(ISubscriber subscriber, String rate) {
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

        //executorService.execute(() -> rateService.manageRawRate(rate));
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
