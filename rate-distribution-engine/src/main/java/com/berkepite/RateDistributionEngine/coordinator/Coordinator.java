package com.berkepite.RateDistributionEngine.coordinator;

import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberConnectionException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberInitException;
import com.berkepite.RateDistributionEngine.common.calculator.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinatorConfig;
import com.berkepite.RateDistributionEngine.common.coordinator.ISubscriberBindingConfig;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberException;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import com.berkepite.RateDistributionEngine.common.rate.IRateManager;
import com.berkepite.RateDistributionEngine.common.rate.IRatesLoader;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriber;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriberLoader;
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
 * <p>
 * On application startup, it loads subscriber classes, manages subscriber connections,
 * and processes incoming rate data.
 * </p>
 * <p>
 * Implements {@link CommandLineRunner} to perform startup logic,
 * and {@link ICoordinator} for coordinator-specific functionality.
 * </p>
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

    private List<ISubscriber> subscribers;

    /**
     * Constructs the Coordinator with the required services and configurations.
     *
     * @param exceptionHandler  Handler for managing exceptions related to subscribers and calculators.
     * @param ratesLoader       Loader that provides the list of rates.
     * @param coordinatorConfig Coordinator configuration object.
     * @param rateManager       Service responsible for managing rate data.
     * @param subscriberLoader  Service for loading subscriber instances.
     * @param executorService   Thread pool executor for managing asynchronous tasks.
     */
    @Autowired
    public Coordinator(ExceptionHandler exceptionHandler, IRatesLoader ratesLoader, ICoordinatorConfig coordinatorConfig, IRateManager rateManager, ISubscriberLoader subscriberLoader, @Qualifier("coordinatorExecutor") ThreadPoolTaskExecutor executorService) {
        this.coordinatorConfig = coordinatorConfig;
        this.subscriberLoader = subscriberLoader;
        this.executorService = executorService;
        this.rateManager = rateManager;
        this.ratesLoader = ratesLoader;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Initializes the coordinator after construction.
     * <p>
     * Loads subscriber classes based on configuration, initializes them,
     * and adds a JVM shutdown hook to properly shut down the executor.
     * </p>
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

            LOGGER.info("Coordinator executor stopped. ({})", this.getClass().getSimpleName());
        }, "shutdown-hook-coordinator"));

    }

    /**
     * Initializes all loaded subscribers.
     * <p>
     * If a subscriber fails to initialize, it is removed from the active subscriber list
     * and the error is handled accordingly.
     * </p>
     */
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
     * Called on application startup.
     * <p>
     * Initiates connection for all loaded subscribers asynchronously.
     * </p>
     *
     * @param args Command line arguments passed to the application.
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
     * Loads subscriber instances from their binding configurations.
     *
     * @param subscriberBindingConfigs List of subscriber binding configurations.
     * @param subscribers              List to populate with loaded subscribers.
     */
    private void loadSubscriberClasses(List<ISubscriberBindingConfig> subscriberBindingConfigs, List<ISubscriber> subscribers) {
        subscriberBindingConfigs.forEach(subscriberBindingConfig -> {
            try {
                if (subscriberBindingConfig.isEnabled()) {
                    ISubscriber subscriber = subscriberLoader.load(subscriberBindingConfig, this);
                    if (subscriber != null) {
                        subscribers.add(subscriber);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }

    /**
     * Called when a subscriber successfully connects.
     * <p>
     * Triggers subscription to the list of available rates asynchronously.
     * </p>
     *
     * @param subscriber The subscriber that connected.
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
     * Called when a subscriber successfully subscribes to rates.
     *
     * @param subscriber The subscriber that subscribed.
     */
    @Override
    public void onSubscribe(ISubscriber subscriber) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} subscribed to {}", config.getName(), config.getUrl());
    }

    /**
     * Called when a subscriber unsubscribes from one or more rates.
     *
     * @param subscriber The subscriber that unsubscribed.
     * @param rates      List of rate names unsubscribed from.
     */
    @Override
    public void onUnSubscribe(ISubscriber subscriber, List<String> rates) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} unsubscribed rates {} from {}", config.getName(), rates.toString(), config.getUrl());
    }

    /**
     * Called when a subscriber disconnects.
     *
     * @param subscriber The subscriber that disconnected.
     */
    @Override
    public void onDisConnect(ISubscriber subscriber) {
        ISubscriberConfig config = subscriber.getConfig();

        LOGGER.info("{} stopped listening/requesting.", config.getName());
    }

    /**
     * Called when a rate becomes available from a subscriber.
     *
     * @param subscriber The subscriber providing the rate.
     * @param rate       The available rate string.
     */
    @Override
    public void onRateAvailable(ISubscriber subscriber, String rate) {
        LOGGER.info("({}) rate available {}", subscriber.getConfig().getName(), rate);
    }

    /**
     * Called when a rate update is received from a subscriber.
     * <p>
     * The update is processed asynchronously by the rate manager unless the executor is shut down.
     * </p>
     *
     * @param subscriber The subscriber providing the rate update.
     * @param rate       The rate update data.
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

    /**
     * Called when a subscriber-related error occurs.
     *
     * @param subscriber The subscriber that caused the error.
     * @param e          The exception representing the error.
     */
    @Override
    public void onSubscriberError(ISubscriber subscriber, SubscriberException e) {
        exceptionHandler.handle(e, subscriber);
    }

    /**
     * Called when a calculator-related error occurs.
     *
     * @param calculator The calculator that caused the error.
     * @param e          The exception representing the error.
     */
    @Override
    public void onCalculatorError(IRateCalculator calculator, CalculatorException e) {
        exceptionHandler.handle(e, calculator);
    }

    /**
     * Connects a subscriber by name.
     *
     * @param subscriberName Name of the subscriber to connect.
     * @return Result message indicating success or failure.
     */
    @Override
    public String connect(String subscriberName) {
        try {
            for (ISubscriber subscriber : subscribers) {
                if (subscriber.getConfig().getName().equals(subscriberName)) {
                    subscriber.connect();
                    return "Connected subscriber: " + subscriberName;
                }
            }
            return "No subscriber found with name: " + subscriberName;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Disconnects a subscriber by name.
     *
     * @param subscriberName Name of the subscriber to disconnect.
     * @return Result message indicating success or failure.
     */
    @Override
    public String disconnect(String subscriberName) {
        try {
            for (ISubscriber subscriber : subscribers) {
                if (subscriber.getConfig().getName().equals(subscriberName)) {
                    subscriber.disConnect();
                    return "Disconnected subscriber: " + subscriberName;
                }
            }
            return "No subscriber found with name: " + subscriberName;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Subscribes a subscriber to specified rates.
     *
     * @param subscriberName Name of the subscriber.
     * @param rates          List of rate names to subscribe to.
     * @return Result message indicating success or failure.
     */
    @Override
    public String subscribe(String subscriberName, List<String> rates) {
        try {
            for (ISubscriber subscriber : subscribers) {
                if (subscriber.getConfig().getName().equals(subscriberName)) {
                    subscriber.subscribe(rates);
                    return subscriberName + " subscribed rates: " + rates;
                }
            }
            return "No subscriber found with name: " + subscriberName;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Unsubscribes a subscriber from specified rates.
     *
     * @param subscriberName Name of the subscriber.
     * @param rates          List of rate names to unsubscribe from.
     * @return Result message indicating success or failure.
     */
    @Override
    public String unSubscribe(String subscriberName, List<String> rates) {
        try {
            for (ISubscriber subscriber : subscribers) {
                if (subscriber.getConfig().getName().equals(subscriberName)) {
                    subscriber.unSubscribe(rates);
                    return subscriberName + " unsubscribed rates: " + rates;
                }
            }
            return "No subscriber found with name: " + subscriberName;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Returns the list of all loaded subscribers.
     *
     * @return List of subscribers.
     */
    @Override
    public List<ISubscriber> getSubscribers() {
        return subscribers;
    }

    /**
     * Returns the coordinator's configuration.
     *
     * @return Coordinator configuration object.
     */
    @Override
    public ICoordinatorConfig getConfig() {
        return coordinatorConfig;
    }
}
