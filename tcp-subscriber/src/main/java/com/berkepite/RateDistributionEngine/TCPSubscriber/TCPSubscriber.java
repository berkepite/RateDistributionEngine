package com.berkepite.RateDistributionEngine.TCPSubscriber;

import com.berkepite.RateDistributionEngine.common.exception.subscriber.*;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriber;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
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
import java.util.concurrent.*;

/**
 * TCPSubscriber connects to a TCP-based rate service to subscribe to currency rate updates.
 * <p>
 * It manages the connection lifecycle including authentication, subscription management,
 * and asynchronous listening for incoming rate data.
 * </p>
 * <p>
 * The subscriber handles retry logic on connection failures and notifies a coordinator
 * about rate updates, connection status, and errors.
 * </p>
 * <p>
 * It supports dynamic subscription and unsubscription to specific rate endpoints,
 * and maintains internal thread pools to process incoming data asynchronously.
 * </p>
 */
public class TCPSubscriber implements ISubscriber {

    private final TCPConfig config;
    private final RateMapper rateMapper;
    private final ICoordinator coordinator;
    private final Logger LOGGER = LogManager.getLogger(TCPSubscriber.class);
    private final ExecutorService executorService;

    private volatile Socket socket;
    private volatile PrintWriter writer;
    private volatile BufferedReader reader;

    private volatile boolean isListeningForInitialResponses = true;
    private volatile boolean isListeningForRates = true;

    /**
     * Constructs a TCPSubscriber with the given coordinator and configuration.
     *
     * @param coordinator the coordinator that handles subscriber events
     * @param config      the subscriber configuration (must be of type {@link TCPConfig})
     */
    public TCPSubscriber(ICoordinator coordinator, ISubscriberConfig config) {
        this.rateMapper = new RateMapper();
        this.config = (TCPConfig) config;
        this.executorService = new ScheduledThreadPoolExecutor(10);
        this.coordinator = coordinator;
    }

    /**
     * Initializes the TCP connection and prepares input/output streams.
     *
     * @throws SubscriberConnectionException if the host cannot be resolved,
     *                                       port is invalid, or I/O error occurs
     * @throws SubscriberException           for any unexpected errors
     */
    @Override
    public void init() throws Exception {
        openSocket();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                socket.close();
            } catch (Exception e) {
                LOGGER.warn("{}", e.getMessage());
            }
            executorService.shutdown();

            isListeningForRates = false;
            isListeningForInitialResponses = false;

            LOGGER.info("Subscriber stopped. ({})", config.getName());
        }, "shutdown-hook-" + config.getName()));
    }

    /**
     * Connects to the TCP server and performs authentication using credentials.
     * Listens for initial authentication responses from the server.
     *
     * @throws Exception if any connection or communication error occurs
     */
    @Override
    public void connect() throws Exception {
        openSocket();

        isListeningForInitialResponses = true;
        isListeningForRates = true;
        String credentials = config.getUsername() + ":" + config.getPassword();
        writer.println(credentials);

        String response;
        while (isListeningForInitialResponses && (response = reader.readLine()) != null) {
            handleInitialResponses(response);
        }
    }

    /**
     * Disconnects from the TCP service by stopping rate listening
     * and notifying the coordinator of disconnection.
     */
    @Override
    public void disConnect() {
        isListeningForInitialResponses = false;
        isListeningForRates = false;

        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.error("IOException has occurred!", e);
        }

        coordinator.onDisConnect(this);
    }

    /**
     * Subscribes to a list of rate types. Combines configured include/exclude lists,
     * maps rate types to endpoints, and sends subscription requests.
     * Starts asynchronous listening for incoming rate updates.
     *
     * @param rates list of rate types to subscribe to
     */
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
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(ratesToSubscribe);

        executorService.execute(this::listen);

        for (String endpoint : endpoints) {
            writer.println("sub|" + endpoint);
        }
    }

    /**
     * Unsubscribes from the specified rates by sending unsubscription requests
     * and notifying the coordinator.
     *
     * @param rates list of rate types to unsubscribe from
     */
    @Override
    public void unSubscribe(List<String> rates) {
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(rates);

        for (String endpoint : endpoints) {
            writer.println("unsub|" + endpoint);
            coordinator.onUnSubscribe(this, List.of(endpoint));
        }
    }

    /**
     * Asynchronous listening loop for incoming rate data.
     * Handles retries with configurable limits and intervals.
     * Notifies coordinator on errors or when retry limit is exceeded.
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
                            executorService.execute(() -> handleResponses(data));
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
                executorService.shutdown();
                disConnect();

                coordinator.onSubscriberError(this,
                        new SubscriberConnectionLostException("The listener (%s) stopped listening because retry limit exceeded.".formatted(config.getName())));
            }
        }
    }

    /**
     * Handles incoming raw data from the TCP server by mapping it to a RawRate object,
     * and notifies the coordinator with the updated rate.
     *
     * @param data raw string data received from the server
     */
    private void handleResponses(final String data) {
        try {
            RawRate rate = rateMapper.createRawRate(data);
            coordinator.onRateUpdate(this, rate);
        } catch (SubscriberRateException e) {
            coordinator.onSubscriberError(this, e);
        }
    }

    /**
     * Processes the initial authentication response from the server.
     * Handles success or failure by adjusting listener state and notifying coordinator.
     *
     * @param response the initial server response string
     */
    private void handleInitialResponses(String response) {
        LOGGER.info("Received the following initial responses: {}", response);

        if (response.equals("AUTH SUCCESS")) {
            isListeningForInitialResponses = false;
            coordinator.onConnect(this);
        } else if (response.equals("AUTH FAILED")) {
            disConnect();
            coordinator.onSubscriberError(this, new SubscriberBadCredentialsException("Authentication failed."));
        }
    }

    private void openSocket() throws SubscriberException {
        try {
            this.socket = new Socket(config.getUrl(), config.getPort());
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            throw new SubscriberConnectionException(
                    "The IP address of the host could not be determined! : %s".formatted(config.getUrl()), e);
        } catch (IllegalArgumentException e) {
            throw new SubscriberConnectionException(
                    "The Port number is out of range : %s (should be 0-65535)!".formatted(config.getPort()), e);
        } catch (IOException e) {
            throw new SubscriberConnectionException("An I/O error has occurred!", e);
        } catch (Exception e) {
            throw new SubscriberException("An unexpected error has occurred!", e);
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

    @Override
    public Logger getLogger() {
        return LOGGER;
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
