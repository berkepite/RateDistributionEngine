package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.coordinator.Coordinator;
import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import com.berkepite.MainApplication32Bit.rates.*;
import com.berkepite.MainApplication32Bit.status.ConnectionStatus;
import com.berkepite.MainApplication32Bit.status.RateStatus;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                socket.shutdownInput();
            } catch (Exception e) {
                LOGGER.info("Socket is probably null : {}", e.getMessage());
            }
            executorService.shutdown();

            isListeningForRates = false;
            isListeningForInitialResponses = false;

            LOGGER.info("Subscriber stopped. ({})", config.getName());
        }, "shutdown-hook-" + config.getName()));
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
            while (isListeningForInitialResponses && (response = reader.readLine()) != null) {

                handleInitialResponses(response);
            }

        } catch (Exception e) {
            ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                    .withSocket(socket, config.getUrl() + ":" + config.getPort())
                    .withException(e)
                    .withSubscriber(this)
                    .withMethod("connect")
                    .build();

            coordinator.onConnectionError(this, connectionStatus);
        }
    }

    @Override
    public void disConnect() {
        isListeningForRates = false;
        coordinator.onDisConnect(this);
    }

    @Override
    public void subscribe(List<RawRateEnum> rates) {
        isListeningForInitialResponses = false;

        List<RawRateEnum> ratesToSubscribe = new ArrayList<>(rates);
        ratesToSubscribe.addAll(config.getIncludeRates());
        ratesToSubscribe.removeAll(config.getExcludeRates());

        LOGGER.info("{} trying to subscribe to {} ", config.getName(), ratesToSubscribe);
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(ratesToSubscribe);

        executorService.execute(this::listen);

        for (String endpoint : endpoints)
            writer.println("sub|" + endpoint);
    }

    @Override
    public void unSubscribe(List<RawRateEnum> rates) {
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(rates);

        for (String endpoint : endpoints)
            writer.println("unsub|" + endpoint);
    }

    private void listen() {
        try {
            String response;
            while (isListeningForRates && (response = reader.readLine()) != null) {
                final String data = response;
                if (!data.isEmpty()) {
                    executorService.execute(() -> handleResponses(data));
                }
            }

        } catch (Exception e) {
            ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                    .withSocket(socket, config.getUrl() + ":" + config.getPort())
                    .withException(e)
                    .withSubscriber(this)
                    .withMethod("listen")
                    .build();

            coordinator.onConnectionError(this, connectionStatus);
        } finally {
            LOGGER.warn("The listener ({}) stopped listening because the server sent null or thread interrupted.", config.getName());
            executorService.shutdown();
        }
    }

    private void handleResponses(final String data) {
        RawRate rate;
        try {
            rate = rateFactory.createRateFromData(SubscriberEnum.CNN_TCP, data);
            coordinator.onRateUpdate(this, rate);
        } catch (Exception e) {
            RateStatus rateStatus = RateStatus.newBuilder()
                    .withData(data)
                    .withMethod("handleResponses")
                    .withSubscriber(this)
                    .withException(e)
                    .build();

            coordinator.onRateError(this, rateStatus);
        }
    }

    private void handleInitialResponses(String response) {
        if (response.equals("AUTH SUCCESS"))
            coordinator.onConnect(this);

        else if (response.equals("AUTH FAILED")) {
            ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                    .withSocket(socket, config.getUrl() + ":" + config.getPort())
                    .withMethod("handleInitialResponses")
                    .withSubscriber(this)
                    .withNotes("Auth failure")
                    .build();

            coordinator.onConnectionError(this, connectionStatus);
        }
    }

}