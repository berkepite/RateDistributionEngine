package com.berkepite.RateDistributionEngine.RestSubscriber;

import com.berkepite.RateDistributionEngine.common.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.ISubscriber;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.ThrowingRunnable;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberConnectionException;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.common.status.ConnectionStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * The BloombergRestSubscriber connects to the Bloomberg REST API to subscribe to rates
 * and handle the communication with the server.
 * It manages connection retries, rate subscriptions, and rate updates.
 */
public class RestSubscriber implements ISubscriber {
    private final RestConfig config;
    private final RateMapper rateMapper;
    private final ICoordinator coordinator;
    private final ExecutorService executorService;
    private final Logger LOGGER = LogManager.getLogger(RestSubscriber.class);
    private HttpClient httpClient;
    private String credentials;

    public RestSubscriber(ICoordinator coordinator, ISubscriberConfig config) {
        this.config = (RestConfig) config;
        this.coordinator = coordinator;
        this.rateMapper = new RateMapper();
        this.httpClient = HttpClient.newBuilder().build();
        this.executorService = new ScheduledThreadPoolExecutor(10);
    }

    /**
     * Initializes the subscriber and prepares it for connection by setting up necessary shutdown hooks and credentials.
     */
    public void init() throws Exception {
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
    public void connect() throws Exception {
        try {
            tryConnect();
        } catch (Exception e) {
            ConnectionStatus status = ConnectionStatus.newBuilder()
                    .withSubscriber(this)
                    .withException(e)
                    .withMethod("RestSubscriber.tryConnect")
                    .withNotes("Health check failed for the platform!")
                    .build();

            LOGGER.info("Subscriber stopped health check. ({}) ", config.getName());

            httpClient.close();

            coordinator.onConnectionError(this, status);
        }
    }

    private void tryConnect() throws SubscriberConnectionException {
        HttpRequest healthRequest = createHealthRequest();

        executeWithRetry("Health Check", config.getHealthRequestRetryLimit(), config.getRequestInterval(), () -> {
            HttpResponse<String> response = httpClient.send(healthRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                coordinator.onConnect(this);
            } else {
                throw new SubscriberConnectionException("Health Check failed (response code: %d)".formatted(response.statusCode()));
            }
        });
    }

    /**
     * Subscribes to a list of rates based on the provided list of rate enums.
     * It sends subscription requests to the Bloomberg REST API.
     *
     * @param rates the list of rates to subscribe to
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

        LOGGER.info("{} subscribing to {} ", config.getName(), ratesToSubscribe);
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(ratesToSubscribe);

        try {
            for (String endpoint : endpoints) {
                trySubscribeToRate(endpoint);
            }
        } catch (SubscriberConnectionException e) {
            ConnectionStatus status = ConnectionStatus.newBuilder()
                    .withSubscriber(this)
                    .withException(e)
                    .withMethod("RestSubscriber.trySubscribeToRate")
                    .withNotes("Subscribing to rate failed for the platform!")
                    .build();

            coordinator.onConnectionError(this, status);
        }

        LOGGER.info("Subscriber stopped trying to subscribe to rates. ({}) ", config.getName());
    }

    private void trySubscribeToRate(String endpoint) throws SubscriberConnectionException {
        // Attempt to subscribe to each rate endpoint
        HttpRequest req = createRateRequest(endpoint);

        executeWithRetry("Subscribing to rate: %s".formatted(endpoint), config.getHealthRequestRetryLimit(), config.getRequestInterval(), () -> {
            HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                coordinator.onSubscribe(this);
                coordinator.onRateAvailable(this, rateMapper.mapEndpointToRateEnum(endpoint));

                // Start the subscription task in a separate thread
                executorService.execute(() -> {
                    try {
                        subscribeToRate(endpoint);
                    } catch (SubscriberConnectionException e) {
                        LOGGER.warn("Shutting down subscriber thread ({}) for rate {} ", Thread.currentThread().getName(), endpoint);
                    }
                });

            } else {
                throw new SubscriberConnectionException("Subscribing to rate: %s failed (response code: %d)".formatted(endpoint, response.statusCode()));
            }
        });


    }

    /**
     * Subscribes to a specific rate endpoint by sending repeated requests with retries.
     *
     * @param endpoint the endpoint to subscribe to
     */
    private void subscribeToRate(String endpoint) throws SubscriberConnectionException {
        HttpRequest req = createRateRequest(endpoint);

        executeWithRetry("Request for rate: %s".formatted(endpoint), config.getRequestRetryLimit(), config.getRequestInterval(), () -> {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.warn("Thread is interrupted. ({})", Thread.currentThread().getName());
                throw new SubscriberConnectionException("Subscribing thread interrupted for rate: %s ".formatted(endpoint));
            }

            while (true) {
                HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    RawRate rate = rateMapper.createRawRate(response.body());
                    coordinator.onRateUpdate(this, rate);
                } else {
                    throw new SubscriberConnectionException("Request for rate: %s failed (response code: %d)".formatted(endpoint, response.statusCode()));
                }

                try {
                    Thread.sleep(config.getRequestInterval());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Thread interrupted during retry cooldown: {}", ex.getMessage());
                }
            }
        });
    }

    @Override
    public void unSubscribe(List<String> rates) {
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

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
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

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private void executeWithRetry(String operationDesc,
                                  int retryLimit,
                                  int intervalMillis,
                                  ThrowingRunnable operation) throws SubscriberConnectionException {
        while (retryLimit >= 0) {
            try {
                operation.run();
                return; // Success, exit retry loop
            } catch (Exception e) {
                Throwable rootCause = getRootCause(e);

                if (retryLimit == 0) {
                    throw new SubscriberConnectionException(
                            "%s failed.".formatted(operationDesc), rootCause
                    );
                }

                LOGGER.warn("{} failed. Remaining retries: {}. Root cause: {}: {}",
                        operationDesc, retryLimit, rootCause.getClass().getSimpleName(), rootCause.getMessage());
                retryLimit--;

                try {
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Thread interrupted during retry cooldown: {}", ex.getMessage());
                    return;
                }
            }
        }
    }

}
