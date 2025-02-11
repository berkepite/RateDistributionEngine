package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.status.ConnectionStatus;
import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;
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
    private final SubscriberLoader subscriberLoader;
    private final CoordinatorConfigLoader coordinatorConfigLoader;
    private final ThreadPoolTaskExecutor executorService;

    private List<ISubscriber> subscribers;

    @Autowired
    public Coordinator(CoordinatorConfigLoader coordinatorConfigLoader, SubscriberLoader subscriberLoader, @Qualifier("coordinatorExecutor") ThreadPoolTaskExecutor executorService) {
        this.coordinatorConfigLoader = coordinatorConfigLoader;
        this.subscriberLoader = subscriberLoader;
        this.executorService = executorService;
    }

    @PostConstruct
    private void init() {
        coordinatorConfig = coordinatorConfigLoader.loadConfig();
        subscribers = new ArrayList<>(3);

        LOGGER.debug("Coordinator Initialized!");
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
    @CoordinatorEventStatus
    public void onConnect(ISubscriber subscriber, ConnectionStatus status) {
        ISubscriberConfig config = subscriber.getConfig();

        switch (status.getStatus()) {
            case OK -> {
                LOGGER.info("{} connected to {}, trying to subscribe...", config.getName(), config.getUrl());
                executorService.execute(() -> subscriber.subscribe(coordinatorConfig.getRates()));
            }
            case AUTHSUCCESS -> {
                LOGGER.info("{} connected to {}, trying to subscribe...", config.getName(), config.getUrl());
                executorService.execute(() -> subscriber.subscribe(coordinatorConfig.getRates()));
            }
            case Interrupted -> LOGGER.warn("{} connection interrupted", config.getName());
            case Unauthorized ->
                    LOGGER.warn("{} could not connect to {} because it could not be AUTHORIZED!", config.getName(), config.getUrl());
            case AUTHFAILED ->
                    LOGGER.warn("{} could not connect to {} because AUTH FAILED!", config.getName(), config.getUrl());
            case IOException ->
                    LOGGER.warn("{} could not connect to {} due to UNEXPECTED IOException!", config.getName(), config.getUrl());
            case ConnectException ->
                    LOGGER.warn("{} could not connect to {} due to UNEXPECTED ConnectException!", config.getName(), config.getUrl());
            case SocketTimeoutException ->
                    LOGGER.warn("{} could not connect to {} because SOCKET TIMEOUT!", config.getName(), config.getUrl());
            case Resource_Not_Found ->
                    LOGGER.warn("{} could not connect to {} because the server returned 404 NOT FOUND!", config.getName(), config.getUrl());
            case Internal_Server_Error ->
                    LOGGER.warn("{} could not connect to {} because the server returned 500 INTERNAL SERVER ERROR!", config.getName(), config.getUrl());
            default ->
                    LOGGER.error("\nAn Unexpected Error occured!\nSubscriber: {}\nConnection: {}", config.toString(), status.toString());

        }
    }

    @Override
    @CoordinatorEventStatus
    public void onSubscribe(ISubscriber subscriber, ConnectionStatus status) {
        ISubscriberConfig config = subscriber.getConfig();

        switch (status.getStatus()) {
            case OK -> LOGGER.info("{} subscribed to {}", config.getName(), status.getUrl());
            case AUTHSUCCESS -> LOGGER.info("{} subscribed to {}", config.getName(), config.getUrl());
            default ->
                    LOGGER.error("\nAn Unexpected Error occured!\nSubscriber: {}\nConnection: {}", config.toString(), status.toString());
        }
    }

    @Override
    @CoordinatorEventStatus
    public void onUnSubscribe(ISubscriber subscriber, ConnectionStatus status) {

    }

    @Override
    @CoordinatorEventStatus
    public void onDisConnect(ISubscriber subscriber) {
        LOGGER.info("{} stopped listening/requesting.", subscriber.getConfig().getName());
    }

    @Override
    public void onRateAvailable(ISubscriber subscriber, RateEnum rate) {
        LOGGER.info("{} rate available", rate);
    }

    @Override
    public void onRateUpdate(ISubscriber subscriber, IRate rate) {
        LOGGER.info("{} rate received", rate.toString());
    }

    @Override
    @CoordinatorEventStatus
    public void onRateError(ISubscriber subscriber, RateStatus status) {
        ISubscriberConfig config = subscriber.getConfig();

        switch (status.getStatus()) {
            case OK ->
                    LOGGER.error("\nAn Unexpected Error occured!\nSubscriber: {}\nStatus: {}", config.toString(), status.toString());
            case Interrupted -> LOGGER.warn("{} connection interrupted", config.getName());
            case Unauthorized ->
                    LOGGER.warn("{} could not get rate from {} because it could not be AUTHORIZED!", config.getName(), status.getUrl());
            case IOException ->
                    LOGGER.warn("{} could not get rate from {} due to UNEXPECTED IOException!", config.getName(), status.getUrl());
            case ConnectException ->
                    LOGGER.warn("{} could not get rate from {} due to UNEXPECTED ConnectException!", config.getName(), status.getUrl());
            case SocketTimeoutException ->
                    LOGGER.warn("{} could not get rate from {} because SOCKET TIMEOUT!", config.getName(), status.getUrl());
            case Resource_Not_Found ->
                    LOGGER.warn("{} could not get rate from {} because the server returned 404 NOT FOUND!", config.getName(), status.getUrl());
            case Internal_Server_Error ->
                    LOGGER.warn("{} could not get rate from {} because the server returned 500 INTERNAL SERVER ERROR!", config.getName(), status.getUrl());
        }
    }

    @Override
    @CoordinatorEventStatus
    public void onConnectionError(ISubscriber subscriber, ConnectionStatus status) {
        ISubscriberConfig config = subscriber.getConfig();

        switch (status.getStatus()) {
            case OK ->
                    LOGGER.error("\nAn Unexpected Error occured!\nSubscriber: {}\nStatus: {}", config.toString(), status.toString());
            case Interrupted -> LOGGER.warn("{} connection interrupted", config.getName());
            case Unauthorized ->
                    LOGGER.warn("{} could not connect to {} because it could not be AUTHORIZED!", config.getName(), config.getUrl());
            case IOException ->
                    LOGGER.warn("{} could not connect to {} due to UNEXPECTED IOException!", config.getName(), config.getUrl());
            case ConnectException ->
                    LOGGER.warn("{} could not connect to {} due to UNEXPECTED ConnectException!", config.getName(), status.getUrl());
            case SocketTimeoutException ->
                    LOGGER.warn("{} could not connect to {} because SOCKET TIMEOUT!", config.getName(), status.getUrl());
            case Resource_Not_Found ->
                    LOGGER.warn("{} could not connect to {} because the server returned 404 NOT FOUND!", config.getName(), status.getUrl());
            case Internal_Server_Error ->
                    LOGGER.warn("{} could not connect to {} because the server returned 500 INTERNAL SERVER ERROR!", config.getName(), config.getUrl());
        }
    }

}
