package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.coordinator.Coordinator;
import com.berkepite.MainApplication32Bit.rates.BloombergRateMapper;
import com.berkepite.MainApplication32Bit.rates.RawRate;
import com.berkepite.MainApplication32Bit.rates.RateFactory;
import com.berkepite.MainApplication32Bit.status.ConnectionStatus;
import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import com.berkepite.MainApplication32Bit.rates.RawRateEnum;
import com.berkepite.MainApplication32Bit.status.RateStatus;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * The BloombergRestSubscriber connects to the Bloomberg REST API to subscribe to rates
 * and handle the communication with the server.
 * It manages connection retries, rate subscriptions, and rate updates.
 */
public class BloombergRestSubscriber implements ISubscriber {
    private final BloombergRestConfig config;
    private final BloombergRateMapper rateMapper;
    private final RateFactory rateFactory;
    private final ICoordinator coordinator;
    private final Logger LOGGER = LogManager.getLogger(BloombergRestSubscriber.class);
    private final ThreadPoolTaskExecutor executorService;
    private String credentials;

    /**
     * Constructor to initialize the BloombergRestSubscriber with necessary dependencies.
     *
     * @param config          the configuration of the Bloomberg REST subscriber
     * @param rateMapper      the rate mapper for mapping rate enums to endpoints
     * @param rateFactory     the factory used for creating rate objects from the API response
     * @param coordinator     the coordinator for notifying about connection and rate updates
     * @param executorService the executor service to handle asynchronous tasks
     */
    public BloombergRestSubscriber(final BloombergRestConfig config, BloombergRateMapper rateMapper, RateFactory rateFactory, Coordinator coordinator, @Qualifier("subscriberExecutor") ThreadPoolTaskExecutor executorService) {
        this.config = config;
        this.coordinator = coordinator;
        this.executorService = executorService;
        this.rateMapper = rateMapper;
        this.rateFactory = rateFactory;
    }

    /**
     * Initializes the subscriber and prepares it for connection by setting up necessary shutdown hooks and credentials.
     */
    @PostConstruct
    public void init() {
        // Shutdown hook to cleanly shut down the executor when the application is stopped
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            LOGGER.info("Subscriber stopped. ({})", config.getName());
        }, "shutdown-hook-" + config.getName()));

        // Create Basic Authorization credentials
        String username_password = config.getUsername() + ":" + config.getPassword();
        credentials = "Basic " + Base64.getEncoder().encodeToString(username_password.getBytes());
    }

    /**
     * Attempts to connect to the Bloomberg REST API and checks the health of the connection.
     * If successful, it notifies the coordinator.
     */
    @Override
    public void connect() {
        HttpRequest req = createHealthRequest();
        HttpClient client = HttpClient.newHttpClient();

        int healthRequestRetryLimit = config.getHealthRequestRetryLimit();
        int requestInterval = config.getRequestInterval();

        // Try connecting until the retry limit is reached
        while (healthRequestRetryLimit > 0) {
            HttpResponse<String> response = null;
            try {
                response = client.send(req, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // Successful connection
                    coordinator.onConnect(this);
                    break;
                } else {
                    // Connection error
                    ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                            .withHttpResponse(response, req)
                            .withMethod("connect")
                            .withSubscriber(this)
                            .withNotes("Response status code was different than 200")
                            .build();

                    coordinator.onConnectionError(this, connectionStatus);
                    healthRequestRetryLimit--;
                }
            } catch (IOException | InterruptedException e) {
                // Handle errors during connection
                ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                        .withHttpRequestError(e, req)
                        .withHttpResponse(response, req)
                        .withMethod("connect")
                        .withSubscriber(this)
                        .build();

                coordinator.onConnectionError(this, connectionStatus);
                healthRequestRetryLimit--;

                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Thread is interrupted. ({}) {}", Thread.currentThread().getName(), e.getMessage());
                }
            }

            // Wait before retrying
            try {
                Thread.sleep(requestInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread is interrupted. ({}) {}", Thread.currentThread().getName(), e.getMessage());
                break;
            }
        }

        LOGGER.info("Subscriber stopped health check. ({}) ", config.getName());
        client.close();
    }

    /**
     * Subscribes to a list of rates based on the provided list of rate enums.
     * It sends subscription requests to the Bloomberg REST API.
     *
     * @param rates the list of rates to subscribe to
     */
    @Override
    public void subscribe(List<RawRateEnum> rates) {
        List<RawRateEnum> ratesToSubscribe = new ArrayList<>(rates);
        ratesToSubscribe.addAll(config.getIncludeRates());
        ratesToSubscribe.removeAll(config.getExcludeRates());

        LOGGER.info("{} subscribing to {} ", config.getName(), ratesToSubscribe);
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(ratesToSubscribe);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Attempt to subscribe to each rate endpoint
        for (String endpoint : endpoints) {
            HttpRequest req = createRateRequest(endpoint);

            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

                // Handle successful subscription
                if (res.statusCode() == 200) {
                    coordinator.onSubscribe(this);
                    coordinator.onRateAvailable(this, rateMapper.mapEndpointToRateEnum(endpoint));

                    // Start the subscription task in a separate thread
                    executorService.execute(() -> subscribeToRate(endpoint));
                } else {
                    // Handle subscription error
                    ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                            .withHttpResponse(res, req)
                            .withMethod("subscribe")
                            .withSubscriber(this)
                            .withNotes("Response status code was different than 200")
                            .build();

                    coordinator.onConnectionError(this, connectionStatus);
                }
            } catch (IOException | InterruptedException e) {
                // Handle subscription request errors
                ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                        .withHttpRequestError(e, req)
                        .withMethod("subscribe")
                        .withSubscriber(this)
                        .build();

                coordinator.onConnectionError(this, connectionStatus);

                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Thread is interrupted. ({})", Thread.currentThread().getName(), e);
                }
            }
        }
        LOGGER.info("Subscriber stopped trying to subscribe to rates. ({}) ", config.getName());
        client.close();
    }

    /**
     * Subscribes to a specific rate endpoint by sending repeated requests with retries.
     *
     * @param endpoint the endpoint to subscribe to
     */
    private void subscribeToRate(String endpoint) {
        int retryLimit = config.getRequestRetryLimit();
        int requestInterval = config.getRequestInterval();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest req = createRateRequest(endpoint);

        // Retry the subscription request until the retry limit is reached
        while (retryLimit > 0) {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.warn("Thread is interrupted. ({})", Thread.currentThread().getName());
                break;
            }

            // Wait before retrying
            try {
                Thread.sleep(requestInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread is interrupted. ({}) {}", Thread.currentThread().getName(), e.getMessage());
                break;
            }

            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

                // Process the response and update the rate
                try {
                    RawRate rate = rateFactory.createRateFromData(SubscriberEnum.BLOOMBERG_REST, res.body());
                    coordinator.onRateUpdate(this, rate);

                } catch (Exception e) {
                    RateStatus rateStatus = RateStatus.newBuilder()
                            .withData(res.body())
                            .withMethod("subscribeToRate")
                            .withSubscriber(this)
                            .withException(e)
                            .withEndpoint(endpoint)
                            .build();

                    coordinator.onRateError(this, rateStatus);

                    retryLimit--;
                }

            } catch (IOException | InterruptedException e) {
                // Handle request errors during the subscription process
                ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                        .withHttpRequestError(e, req)
                        .withMethod("subscribeToRate")
                        .withSubscriber(this)
                        .build();

                coordinator.onConnectionError(this, connectionStatus);

                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Thread is interrupted. ({})", Thread.currentThread().getName());
                    break;
                }

                retryLimit--;
            }
        }
        LOGGER.info("Shutting down subscriber task. ({})", Thread.currentThread().getName());
        client.close();
    }

    @Override
    public void unSubscribe(List<RawRateEnum> rates) {
        // Implementation for unsubscription
    }

    @Override
    public void disConnect() {
        // Implementation for disconnect
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
     * Creates an HTTP request to subscribe to a specific rate endpoint.
     *
     * @param endpoint the endpoint to send the request to
     * @return the constructed HTTP request
     */
    private HttpRequest createRateRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl() + "/api/currencies/" + endpoint))
                .setHeader("Authorization", credentials)
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Creates an HTTP health check request.
     *
     * @return the constructed HTTP health check request
     */
    private HttpRequest createHealthRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl() + "/api/health"))
                .setHeader("Authorization", credentials)
                .GET()
                .build();
    }
}
