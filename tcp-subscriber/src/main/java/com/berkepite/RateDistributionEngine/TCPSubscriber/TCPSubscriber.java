package com.berkepite.RateDistributionEngine.TCPSubscriber;

import com.berkepite.RateDistributionEngine.common.exception.RateMapperException;
import com.berkepite.RateDistributionEngine.common.ISubscriber;
import com.berkepite.RateDistributionEngine.common.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberBadCredentialsException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberConnectionException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberException;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.common.status.ConnectionStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * TCPSubscriber is a subscriber that connects to the  TCP service to subscribe to rate updates.
 * It listens for incoming rate data and handles connection errors and subscription management.
 */

public class TCPSubscriber implements ISubscriber {
    private final TCPConfig config;
    private final RateMapper rateMapper;
    private final ICoordinator coordinator;
    private final Logger LOGGER = LogManager.getLogger(TCPSubscriber.class);
    private final ExecutorService executorService;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private volatile boolean isListeningForInitialResponses = true;
    private volatile boolean isListeningForRates = true;

    public TCPSubscriber(ICoordinator coordinator, ISubscriberConfig config) {
        this.rateMapper = new RateMapper();
        this.config = (TCPConfig) config;
        this.executorService = new ScheduledThreadPoolExecutor(10);
        this.coordinator = coordinator;
    }

    @Override
    public void init() throws Exception {
        try {
            this.socket = new Socket(config.getUrl(), config.getPort());
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            throw new SubscriberConnectionException("The IP address of the host could not be determined! : %s".formatted(config.getUrl()), e);
        } catch (IllegalArgumentException e) {
            throw new SubscriberConnectionException("The Port number is out of range : %s (should be 0-65535)!".formatted(config.getPort()), e);
        } catch (IOException e) {
            throw new SubscriberConnectionException("An I/O error has occurred!", e);
        } catch (Exception e) {
            throw new SubscriberException("An unexpected error has occurred!", e);
        }

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


    /**
     * Connects to the CNN TCP server using the provided URL and port.
     * Handles initial authentication and prepares to listen for rate updates.
     */
    @Override
    public void connect() throws Exception {
        String credentials = config.getUsername() + ":" + config.getPassword();
        writer.println(credentials);  // Send credentials for authentication

        // Listen for the server's initial response
        String response;
        while (isListeningForInitialResponses && (response = reader.readLine()) != null) {
            handleInitialResponses(response); // Handle the response from the server
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
        List<String> ratesToSubscribe = new ArrayList<>(rates);
        ratesToSubscribe.addAll(config.getIncludeRates());
        ratesToSubscribe.removeAll(config.getExcludeRates());

        if (ratesToSubscribe.isEmpty()) {
            LOGGER.warn("{} received empty rates to subscribe {}. Aborting...", config.getName(), ratesToSubscribe);
            return;
        }

        LOGGER.info("{} trying to subscribe to {} ", config.getName(), ratesToSubscribe);
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(ratesToSubscribe);  // Map rate enums to endpoints

        executorService.execute(this::listen);

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
        int retryLimit = config.getRequestRetryLimit();
        int intervalMillis = config.getRequestInterval();

        while (retryLimit >= 0) {
            try {
                String response;
                while (isListeningForRates) {
                    if ((response = reader.readLine()) != null) {
                        final String data = response;
                        if (!data.isEmpty()) {
                            executorService.execute(() -> handleResponses(data));  // Process the incoming data
                        }
                    } else {
                        throw new SubscriberConnectionException("Server sent null.");
                    }
                }

            } catch (IOException e) {
                LOGGER.warn("An I/O error occurred. Remaining retries: {}", retryLimit, e);
                retryLimit--;
            } catch (SubscriberConnectionException e) {
                LOGGER.warn("{} Remaining retries: {}", e.getMessage(), retryLimit);
                retryLimit--;
            } catch (Exception e) {
                LOGGER.error("An unexpected error occurred. Remaining retries: {}", retryLimit, e);
                retryLimit--;
            }

            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread interrupted during retry cooldown: {}", e.getMessage());
            }

            if (retryLimit < 0) {
                LOGGER.warn("The listener ({}) stopped listening because retry limit exceeded.", config.getName());
                executorService.shutdown();  // Shutdown the executor when done listening

                ConnectionStatus status = ConnectionStatus.newBuilder()
                        .withSubscriber(this)
                        .withSocket(socket, config.getUrl() + ":" + config.getPort())
                        .withMethod("listen")
                        .build();

                coordinator.onConnectionError(this, status);
            }
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
        } catch (RateMapperException e) {
            LOGGER.warn(e);
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred.", e);
        }
    }

    /**
     * Handles the initial responses from the server, including authentication success or failure.
     *
     * @param response the response received from the server
     */
    private void handleInitialResponses(String response) throws SubscriberConnectionException {
        LOGGER.info("Received the following initial responses: {}", response);

        if (response.equals("AUTH SUCCESS")) {
            isListeningForInitialResponses = false;  // Stop listening for initial responses after subscription
            coordinator.onConnect(this);  // Notify the coordinator if authentication is successful
        } else if (response.equals("AUTH FAILED")) {
            throw new SubscriberBadCredentialsException("Response is AUTH FAILED, check for bad credentials!");
        }
    }

    @Override
    public ICoordinator getCoordinator() {
        return coordinator;
    }

    @Override
    public ISubscriberConfig getConfig() {
        return config;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }
}
