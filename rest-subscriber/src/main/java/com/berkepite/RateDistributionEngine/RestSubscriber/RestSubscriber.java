package com.berkepite.RateDistributionEngine.RestSubscriber;

import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberException;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriber;
import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import com.berkepite.RateDistributionEngine.common.ThrowingRunnable;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberConnectionException;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * RestSubscriber is responsible for subscribing to currency rate updates from a REST API.
 * It manages HTTP connections, subscription lifecycle, and forwards rate updates to a coordinator.
 * <p>
 * This class handles connection health checks, subscription requests, rate update polling,
 * and retries on failure with configurable limits and intervals.
 */
public class RestSubscriber implements ISubscriber {
    private static final Logger LOGGER = LogManager.getLogger(RestSubscriber.class);
    private final RestConfig config;
    private final RateMapper rateMapper;
    private final ICoordinator coordinator;
    private final ExecutorService executorService;
    private HttpClient httpClient;
    private String credentials;

    private volatile Map<String, Boolean> ratesToSubscribe;
    private volatile boolean isRequesting = false;

    /**
     * Constructs a RestSubscriber with the given coordinator and subscriber configuration.
     *
     * @param coordinator The coordinator to notify about subscription events and rate updates.
     * @param config      The subscriber configuration (cast to RestConfig).
     */
    public RestSubscriber(ICoordinator coordinator, ISubscriberConfig config) {
        this.config = (RestConfig) config;
        this.coordinator = coordinator;
        this.rateMapper = new RateMapper();
        this.httpClient = HttpClient.newBuilder().build();
        this.executorService = new ScheduledThreadPoolExecutor(10);
        this.ratesToSubscribe = new HashMap<>(3);
    }

    /**
     * Initializes the subscriber, setting up credentials and shutdown hook.
     *
     * @throws Exception If initialization fails.
     */
    public void init() throws Exception {
        try {
            // Shutdown hook to cleanly shut down the executor when the application is stopped
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                executorService.shutdown();
                LOGGER.info("Subscriber stopped. ({})", config.getName());
            }, "shutdown-hook-" + config.getName()));

            // Create Basic Authorization credentials
            String username_password = config.getUsername() + ":" + config.getPassword();
            credentials = "Basic " + Base64.getEncoder().encodeToString(username_password.getBytes());
        } catch (Exception e) {
            throw new SubscriberException("Something went wrong: ", e);
        }
    }

    /**
     * Connects to the REST API by performing a health check.
     *
     * @throws Exception If connection or health check fails.
     */
    @Override
    public void connect() throws Exception {
        try {
            tryConnect();
        } catch (SubscriberConnectionException e) {
            disConnect();
            coordinator.onSubscriberError(this, e);
        }
    }

    /**
     * Attempts to connect by sending a health check request with retry logic.
     *
     * @throws SubscriberException If health check fails after retries.
     */
    private void tryConnect() throws SubscriberException {
        HttpRequest healthRequest = createHealthRequest();

        executeWithRetry("Health Check", config.getHealthRequestRetryLimit(), config.getRequestInterval(), () -> {
            HttpResponse<String> response = httpClient.send(healthRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                coordinator.onConnect(this);
            } else {
                LOGGER.warn("Subscriber stopped health check. ({}) ", config.getName());
                throw new SubscriberConnectionException("Health Check failed (response code: %d)".formatted(response.statusCode()));
            }
        });

    }

    /**
     * Subscribes to the given list of currency rates.
     * Starts polling for rate updates in separate threads.
     *
     * @param rates List of rate identifiers to subscribe to.
     */
    @Override
    public void subscribe(List<String> rates) {
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(rates);

        endpoints.forEach(rate -> ratesToSubscribe.put(rate, true));
        config.getIncludeRates().forEach(rate -> ratesToSubscribe.put(rate, true));
        config.getExcludeRates().forEach(ratesToSubscribe::remove);

        if (ratesToSubscribe.isEmpty()) {
            LOGGER.warn("{} received empty rates to subscribe {}. Aborting...", config.getName(), ratesToSubscribe);
            return;
        }

        LOGGER.info("{} trying to subscribe to {} ", config.getName(), ratesToSubscribe);

        try {
            for (String endpoint : endpoints) {
                trySubscribeToRate(endpoint);
            }
        } catch (SubscriberException e) {
            coordinator.onSubscriberError(this, e);
        }
    }

    /**
     * Tries to subscribe to a single rate endpoint, with retries.
     * Starts a background thread to keep polling the rate updates.
     *
     * @param endpoint The rate endpoint to subscribe to.
     * @throws SubscriberException If subscription fails.
     */
    private void trySubscribeToRate(String endpoint) throws SubscriberException {
        HttpRequest req = createRateRequest(endpoint);

        executeWithRetry("Subscribing to rate: %s".formatted(endpoint), config.getHealthRequestRetryLimit(), config.getRequestInterval(), () -> {
            HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                coordinator.onSubscribe(this);
                coordinator.onRateAvailable(this, rateMapper.mapEndpointToRateEnum(endpoint));

                // Start the subscription task in a separate thread
                executorService.execute(() -> {
                    try {
                        isRequesting = true;
                        subscribeToRate(endpoint);
                    } catch (SubscriberException e) {
                        LOGGER.warn("Shutting down subscriber thread ({}) for rate {} ", Thread.currentThread().getName(), endpoint);
                    }
                });

            } else {
                throw new SubscriberConnectionException("Subscribing to rate: %s failed (response code: %d)".formatted(endpoint, response.statusCode()));
            }
        });
    }

    /**
     * Polls the rate endpoint repeatedly to get updates and notify the coordinator.
     * Stops polling when unsubscribed or thread interrupted.
     *
     * @param endpoint The rate endpoint to poll.
     * @throws SubscriberException If requests fail or thread is interrupted.
     */
    private void subscribeToRate(String endpoint) throws SubscriberException {
        HttpRequest req = createRateRequest(endpoint);

        executeWithRetry("Request for rate: %s".formatted(endpoint), config.getRequestRetryLimit(), config.getRequestInterval(), () -> {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.warn("Thread is interrupted. ({})", Thread.currentThread().getName());
                throw new SubscriberConnectionException("Subscribing thread interrupted for rate: %s ".formatted(endpoint));
            }

            while (isRequesting && ratesToSubscribe.get(endpoint) && !Thread.currentThread().isInterrupted()) {
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

    /**
     * Unsubscribes from the given list of currency rates.
     * Stops polling the rates and notifies the coordinator.
     *
     * @param rates List of rate identifiers to unsubscribe from.
     */
    @Override
    public void unSubscribe(List<String> rates) {
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(rates);

        endpoints.forEach(rate -> ratesToSubscribe.put(rate, false));

        coordinator.onUnSubscribe(this, rates);
    }

    /**
     * Disconnects the subscriber by stopping all ongoing requests and closing the HTTP client.
     */
    @Override
    public void disConnect() {
        isRequesting = false;

        coordinator.onDisConnect(this);
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

    /**
     * Sets a custom HttpClient, mainly for testing purposes.
     *
     * @param httpClient The HttpClient instance to use.
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Creates an HTTP request for a specific currency rate endpoint.
     *
     * @param endpoint The rate endpoint to request.
     * @return The constructed HttpRequest.
     * @throws SubscriberException If request creation fails.
     */
    private HttpRequest createRateRequest(String endpoint) throws SubscriberException {
        try {
            return HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl() + "/api/currencies/" + endpoint))
                    .setHeader("Authorization", credentials)
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
        } catch (Exception e) {
            throw new SubscriberException("Failed to create Rate Request!", e);
        }
    }

    /**
     * Creates an HTTP request to check the health of the REST API.
     *
     * @return The constructed health check HttpRequest.
     * @throws SubscriberException If request creation fails.
     */
    private HttpRequest createHealthRequest() throws SubscriberException {
        try {
            return HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl() + "/api/health"))
                    .setHeader("Authorization", credentials)
                    .GET()
                    .build();
        } catch (Exception e) {
            throw new SubscriberException("Failed to create Health Request!", e);
        }
    }

    /**
     * Finds the root cause of a throwable by traversing the cause chain.
     *
     * @param throwable The throwable to analyze.
     * @return The root cause throwable.
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Executes an operation with retry logic.
     * Retries the operation up to retryLimit times, waiting intervalMillis between attempts.
     *
     * @param operationDesc  Description of the operation (for logging).
     * @param retryLimit     Number of retries allowed.
     * @param intervalMillis Wait interval between retries in milliseconds.
     * @param operation      The operation to execute that may throw exceptions.
     * @throws SubscriberConnectionException If all retries fail.
     */
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
