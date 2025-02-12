package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.coordinator.Coordinator;
import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import com.berkepite.MainApplication32Bit.rates.*;
import com.berkepite.MainApplication32Bit.status.ConnectionStatus;
import com.berkepite.MainApplication32Bit.status.RateStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CNNTCPSubscriber implements ISubscriber {
    private final CNNTCPConfig config;
    private final CNNRateMapper rateMapper;
    private final RateFactory rateFactory;
    private final ICoordinator coordinator;
    private final Logger LOGGER = LogManager.getLogger(CNNTCPSubscriber.class);
    private final ThreadPoolTaskExecutor executorService;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private volatile boolean isListeningForInitialResponses = true;
    private volatile boolean isListeningForRates = true;

    public CNNTCPSubscriber(CNNTCPConfig config, CNNRateMapper rateMapper, RateFactory rateFactory, Coordinator coordinator, @Qualifier("subscriberExecutor") ThreadPoolTaskExecutor executorService) {
        this.config = config;
        this.coordinator = coordinator;
        this.executorService = executorService;
        this.rateMapper = rateMapper;
        this.rateFactory = rateFactory;
    }

    @Override
    public ICoordinator getCoordinator() {
        return coordinator;
    }

    @Override
    public ISubscriberConfig getConfig() {
        return config;
    }

    @Override
    public void connect() {
        try {
            this.socket = new Socket(config.getUrl(), config.getPort());
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String credentials = config.getUsername() + ":" + config.getPassword();
            writer.println(credentials);

            String response;
            while ((response = reader.readLine()) != null && isListeningForInitialResponses) {

                handleInitialResponses(response);
            }

        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred!: {}", e.getMessage());
        }
    }

    @Override
    public void disConnect() {
        isListeningForRates = false;
        coordinator.onDisConnect(this);
    }

    @Override
    public void subscribe(List<RateEnum> rates) {
        isListeningForInitialResponses = false;

        List<RateEnum> ratesToSubscribe = new ArrayList<>(rates);
        ratesToSubscribe.addAll(config.getIncludeRates());
        ratesToSubscribe.removeAll(config.getExcludeRates());

        LOGGER.info("{} trying to subscribe to {} ", config.getName(), ratesToSubscribe);
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(ratesToSubscribe);

        executorService.execute(this::listen);

        for (String endpoint : endpoints)
            writer.println("sub|" + endpoint);
    }

    @Override
    public void unSubscribe(List<RateEnum> rates) {
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(rates);

        for (String endpoint : endpoints)
            writer.println("unsub|" + endpoint);
    }

    private void listen() {
        try {
            String response;
            while ((response = reader.readLine()) != null && isListeningForRates) {
                final String data = response;
                if (!data.isEmpty())
                    executorService.execute(() -> handleResponses(data));
            }

        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred!: ", e);
        } finally {
            LOGGER.info("The listener ({}) stopped listening...", config.getName());
            executorService.shutdown();
        }
    }

    private void handleResponses(final String data) {
        IRate rate;
        try {
            rate = rateFactory.createRate(SubscriberEnum.CNN_TCP, data);
            coordinator.onRateUpdate(this, rate);
        } catch (Exception e) {
            coordinator.onRateError(this, new RateStatus(data, e));
        }
    }

    private void handleInitialResponses(String response) {
        if (response.equals("AUTH SUCCESS"))
            coordinator.onConnect(this, new ConnectionStatus(socket, response));
        else if (response.equals("AUTH FAILED"))
            coordinator.onConnectionError(this, new ConnectionStatus(socket, response));
    }

}