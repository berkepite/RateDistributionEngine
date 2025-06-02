package com.berkepite.RateDistributionEngine.coordinator;

import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberConnectionException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberInitException;
import com.berkepite.RateDistributionEngine.email.EmailService;
import com.berkepite.RateDistributionEngine.common.calculators.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinatorConfig;
import com.berkepite.RateDistributionEngine.common.coordinator.ISubscriberBindingConfig;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberException;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.common.rates.IRateManager;
import com.berkepite.RateDistributionEngine.common.rates.IRatesLoader;
import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriber;
import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriberLoader;
import com.berkepite.RateDistributionEngine.exception.ExceptionHandler;
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

    private final ICoordinatorConfig coordinatorConfig;
    private final IRateManager rateManager;
    private final ISubscriberLoader subscriberLoader;
    private final IRatesLoader ratesLoader;
    private final ThreadPoolTaskExecutor executorService;
    private final ExceptionHandler exceptionHandler;
    private final EmailService emailService;

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
    public Coordinator(ExceptionHandler exceptionHandler, IRatesLoader ratesLoader, ICoordinatorConfig coordinatorConfig, IRateManager rateManager, ISubscriberLoader subscriberLoader, @Qualifier("coordinatorExecutor") ThreadPoolTaskExecutor executorService, EmailService emailService) {
        this.coordinatorConfig = coordinatorConfig;
        this.subscriberLoader = subscriberLoader;
        this.executorService = executorService;
        this.rateManager = rateManager;
        this.ratesLoader = ratesLoader;
        this.exceptionHandler = exceptionHandler;
        this.emailService = emailService;
    }

    /**
     * Initializes the coordinator by setting up shutdown hooks.
     * Initializes the subscriber list
     * This method is called after the constructor.
     */
    @PostConstruct
    private void init() {
        subscribers = new ArrayList<>(2);

        loadSubscriberClasses(coordinatorConfig.getSubscriberBindings(), subscribers);

        if (!subscribers.isEmpty()) {
            LOGGER.info("Subscriber classes loaded!: {}", subscribers.toString());
        } else {
            LOGGER.error("0 subscriber classes loaded! Exiting...");
            return;
        }
        LOGGER.info("Coordinator Initialized!");

        initSubscribers();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();

            LOGGER.info("Executor stopped. ({})", this.getClass().getSimpleName());
        }, "shutdown-hook-coordinator"));

    }

    private void initSubscribers() {
        Iterator<ISubscriber> iterator = subscribers.iterator();

        while (iterator.hasNext()) {
            ISubscriber subscriber = iterator.next();
            try {
                subscriber.init();
            } catch (Exception e) {
                LOGGER.error("Failed to init subscriber class! {} Cause: {}", subscriber.getConfig().getClassName(), e);

                exceptionHandler.handle(new SubscriberInitException("Failed to init subscriber class (%s)".formatted(subscriber.getConfig().getClassName()), e), subscriber);

                LOGGER.warn("Removing {} from subscribers!", subscriber.getConfig().getClassName());
                iterator.remove();
            }
        }
    }

    /**
     * Runs the coordinator by binding subscribers and initiating connections.
     *
     * @param args command line arguments passed to the application
     */
    @Override
    public void run(String... args) {
        for (ISubscriber subscriber : subscribers) {
            executorService.execute(() -> {
                try {
                    subscriber.connect();
                } catch (Exception e) {
                    exceptionHandler.handle(new SubscriberConnectionException("Subscriber (%s) failed to connect to platform (%s)".formatted(subscriber.getConfig().getClassName(), subscriber.getConfig().getUrl()), e), subscriber);
                }
            });
        }
    }

    /**
     * Loads subscriber classes based on the provided configurations.
     *
     * @param subscriberBindingConfigs list of subscriber binding configurations
     */
    private void loadSubscriberClasses(List<ISubscriberBindingConfig> subscriberBindingConfigs, List<ISubscriber> subscribers) {
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
     * Handles the connection event for a subscriber.
     *
     * @param subscriber the subscriber that has connected
     */
    @Override
    public void onConnect(ISubscriber subscriber) {
        ISubscriberConfig config = subscriber.getConfig();
        LOGGER.info("Subscriber rates: {}", ratesLoader.getRatesList());

        LOGGER.info("{} connected to {}, trying to subscribe...", config.getName(), config.getUrl());
        executorService.execute(() -> {
            try {
                subscriber.subscribe(ratesLoader.getRatesList());
            } catch (Exception e) {
                exceptionHandler.handle(new SubscriberException("Subscriber (%s) failed to subscribe to rates (%s)".formatted(subscriber.getConfig().getName(), ratesLoader.getRatesList()), e), subscriber);
            }
        });
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
     * @param rates      d
     */
    @Override
    public void onUnSubscribe(ISubscriber subscriber, List<String> rates) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} unsubscribed rates {} from {}", config.getName(), rates.toString(), config.getUrl());
    }

    /**
     * Handles the disconnection event for a subscriber.
     *
     * @param subscriber the subscriber that has disconnected
     */
    @Override
    public void onDisConnect(ISubscriber subscriber) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} stopped listening/requesting.", config.getName());
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
        try {
            LOGGER.info("({}) rate received {}", subscriber.getConfig().getName(), rate.toString());

            if (!executorService.getThreadPoolExecutor().isShutdown()) {
                executorService.execute(() -> rateManager.manageIncomingRawRate(rate));
            } else {
                LOGGER.warn("Executor is shut down. Dropping rate: {}", rate);
            }

        } catch (Exception e) {
            LOGGER.error("Something went wrong: ", e);
        }
    }

    @Override
    public void onSubscriberError(ISubscriber subscriber, SubscriberException e) {
        exceptionHandler.handle(e, subscriber);
    }

    @Override
    public void onCalculatorError(IRateCalculator calculator, CalculatorException e) {
        exceptionHandler.handle(e, calculator);
    }

    /**
     * Returns the list of subscribers.
     *
     * @return the list of subscribers
     */
    @Override
    public List<ISubscriber> getSubscribers() {
        return subscribers;
    }

    /**
     * Returns the coordinator configuration.
     *
     * @return the coordinator configuration
     */
    @Override
    public ICoordinatorConfig getConfig() {
        return coordinatorConfig;
    }
}
