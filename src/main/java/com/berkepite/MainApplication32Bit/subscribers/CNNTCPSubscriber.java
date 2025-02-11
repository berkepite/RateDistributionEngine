package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import com.berkepite.MainApplication32Bit.rates.CNNRate;
import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.status.ConnectionStatus;
import com.berkepite.MainApplication32Bit.status.RateStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CNNTCPSubscriber implements ISubscriber {
    private final CNNTCPConfig config;
    private ICoordinator coordinator;
    private final Logger LOGGER = LogManager.getLogger(CNNTCPSubscriber.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private volatile boolean isListeningForInitialResponses = true;
    private volatile boolean isListeningForRates = true;

    public CNNTCPSubscriber(final CNNTCPConfig config) {
        this.config = config;
    }

    @Override
    public ICoordinator getCoordinator() {
        return coordinator;
    }

    @Override
    public void setCoordinator(ICoordinator coordinator) {
        this.coordinator = coordinator;
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
        List<String> endpoints = mapRateEnumToEndpoints(ratesToSubscribe);

        executorService.execute(this::listen);

        for (String endpoint : endpoints)
            writer.println("sub|" + endpoint);
    }

    @Override
    public void unSubscribe(List<RateEnum> rates) {
        List<String> endpoints = mapRateEnumToEndpoints(rates);

        for (String endpoint : endpoints)
            writer.println("unsub|" + endpoint);
    }

    private void listen() {
        try {
            String response;
            while ((response = reader.readLine()) != null && isListeningForRates) {
                final String data = response;
                executorService.execute(() -> handleResponses(data));
            }

        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred!: ", e);
        } finally {
            LOGGER.info("The listener ({}) stopped listening...", config.getName());
            executorService.shutdown();
        }
    }

    private void handleResponses(String data) {
        if (data.startsWith("name=")) {
            IRate rate;
            if ((rate = createRate(data)) != null) {
                coordinator.onRateUpdate(this, rate);
            } else {
                coordinator.onRateError(this, new RateStatus(socket, data));
            }
        }
    }

    private void handleInitialResponses(String response) {
        if (response.equals("AUTH SUCCESS"))
            coordinator.onConnect(this, new ConnectionStatus(socket, response));
        else if (response.equals("AUTH FAILED"))
            coordinator.onConnectionError(this, new ConnectionStatus(socket, response));
    }

    public CNNRate createRate(String data) {
        try {
            CNNRate rate = new CNNRate();
            List<String> fields = Arrays.stream(data.split("\\|")).toList();

            List<String> nameField = Arrays.stream(fields.getFirst().split("=")).toList();
            List<String> bidField = Arrays.stream(fields.get(1).split("=")).toList();
            List<String> askField = Arrays.stream(fields.get(2).split("=")).toList();
            List<String> timestampField = Arrays.stream(fields.get(3).split("=")).toList();

            rate.setType(mapEndpointToRateEnum(nameField.get(1)));
            rate.setAsk(Double.parseDouble(askField.get(1)));
            rate.setBid(Double.parseDouble(bidField.get(1)));
            rate.setTimeStamp(Instant.parse(timestampField.get(1)));

            return rate;

        } catch (Exception e) {
            return null;
        }
    }

    private static String mapRateEnumToEndpoint(RateEnum rate) {
        String endpoint;
        endpoint = rate.toString().replace("_", "");

        return endpoint;
    }

    private static List<String> mapRateEnumToEndpoints(List<RateEnum> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    private static RateEnum mapEndpointToRateEnum(String rateStr) {
        RateEnum rate;
        rate = RateEnum.valueOf(rateStr.substring(0, 3) + "_" + rateStr.substring(3));

        return rate;
    }

}