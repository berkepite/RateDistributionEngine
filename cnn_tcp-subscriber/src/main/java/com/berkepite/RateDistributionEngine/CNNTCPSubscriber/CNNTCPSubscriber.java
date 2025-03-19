package com.berkepite.RateDistributionEngine.CNNTCPSubscriber;

import com.berkepite.RateDistributionEngine.common.ISubscriber;
import com.berkepite.RateDistributionEngine.common.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.common.status.ConnectionStatus;
import com.berkepite.RateDistributionEngine.common.status.RateStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * CNNTCPSubscriber is a subscriber that connects to the CNN TCP service to subscribe to rate updates.
 * It listens for incoming rate data and handles connection errors and subscription management.
 */
public class CNNTCPSubscriber implements ISubscriber {
    private CNNTCPConfig config;
    private final CNNTCPConfigLoader configLoader;
    private final CNNRateMapper rateMapper;
    private final ICoordinator coordinator;
    private final Logger LOGGER = LogManager.getLogger(CNNTCPSubscriber.class);
    private final ThreadPoolTaskExecutor executorService;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private volatile boolean isListeningForInitialResponses = true;
    private volatile boolean isListeningForRates = true;

    public CNNTCPSubscriber(ICoordinator coordinator, ThreadPoolTaskExecutor executorService) {
        this.configLoader = new CNNTCPConfigLoader();
        this.rateMapper = new CNNRateMapper();
        this.executorService = executorService;
        this.coordinator = coordinator;
        init();
    }

    public void init() {
        this.config = configLoader.load();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                socket.shutdownInput(); // Shutdown input stream
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

    /**
     * Connects to the CNN TCP server using the provided URL and port.
     * Handles initial authentication and prepares to listen for rate updates.
     */
    @Override
    public void connect() {
        try {
            this.socket = new Socket(config.getUrl(), config.getPort());
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String credentials = config.getUsername() + ":" + config.getPassword();
            writer.println(credentials);  // Send credentials for authentication

            // Listen for the server's initial response
            String response;
            while (isListeningForInitialResponses && (response = reader.readLine()) != null) {
                handleInitialResponses(response); // Handle the response from the server
            }

        } catch (Exception e) {
            ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                    .withSocket(socket, config.getUrl() + ":" + config.getPort())
                    .withException(e)
                    .withSubscriber(this)
                    .withMethod("connect")
                    .build();

            coordinator.onConnectionError(this, connectionStatus);  // Notify the coordinator about the error
        }
    }

    /**
     * Disconnects from the CNN TCP service.
     * Stops listening for rate updates and notifies the coordinator.
     */
    @Override
    public void disConnect() {
        isListeningForRates = false;
        coordinator.onDisConnect(this);
    }

    @Override
    public void subscribe(List<String> rates) {
        isListeningForInitialResponses = false;  // Stop listening for initial responses after subscription

        List<String> ratesToSubscribe = new ArrayList<>(rates);
        ratesToSubscribe.addAll(config.getIncludeRates());
        ratesToSubscribe.removeAll(config.getExcludeRates());

        LOGGER.info("{} trying to subscribe to {} ", config.getName(), ratesToSubscribe);
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(ratesToSubscribe);  // Map rate enums to endpoints

        executorService.execute(this::listen);  // Start a listener for incoming rates

        for (String endpoint : endpoints) {
            writer.println("sub|" + endpoint);  // Send subscription request to the server
        }
    }

    @Override
    public void unSubscribe(List<String> rates) {
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(rates);

        for (String endpoint : endpoints) {
            writer.println("unsub|" + endpoint);  // Send unsubscription request to the server
        }
    }

    /**
     * Listens for incoming rate data from the server asynchronously.
     * Reads data from the server and handles the responses.
     */
    private void listen() {
        try {
            String response;
            while (isListeningForRates && (response = reader.readLine()) != null) {
                final String data = response;
                if (!data.isEmpty()) {
                    executorService.execute(() -> handleResponses(data));  // Process the incoming data
                }
            }

        } catch (Exception e) {
            ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                    .withSocket(socket, config.getUrl() + ":" + config.getPort())
                    .withException(e)
                    .withSubscriber(this)
                    .withMethod("listen")
                    .build();

            coordinator.onConnectionError(this, connectionStatus);  // Notify the coordinator about the error
        } finally {
            LOGGER.warn("The listener ({}) stopped listening because the server sent null or thread interrupted.", config.getName());
            executorService.shutdown();  // Shutdown the executor when done listening
        }
    }

    /**
     * Handles incoming rate data by creating a RawRate object and notifying the coordinator.
     *
     * @param data the raw data received from the server
     */
    private void handleResponses(final String data) {
        try {
            RawRate rate = rateMapper.createRawRate(data);
            coordinator.onRateUpdate(this, rate);
        } catch (Exception e) {
            RateStatus rateStatus = RateStatus.newBuilder()
                    .withData(data)
                    .withMethod("handleResponses")
                    .withSubscriber(this)
                    .withException(e)
                    .build();

            coordinator.onRateError(this, rateStatus);  // Notify the coordinator if an error occurs
        }
    }

    /**
     * Handles the initial responses from the server, including authentication success or failure.
     *
     * @param response the response received from the server
     */
    private void handleInitialResponses(String response) {
        if (response.equals("AUTH SUCCESS")) {
            coordinator.onConnect(this);  // Notify the coordinator if authentication is successful
        } else if (response.equals("AUTH FAILED")) {
            ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                    .withSocket(socket, config.getUrl() + ":" + config.getPort())
                    .withMethod("handleInitialResponses")
                    .withSubscriber(this)
                    .withNotes("Auth failure")
                    .build();

            coordinator.onConnectionError(this, connectionStatus);  // Notify the coordinator if authentication fails
        }
    }
}
