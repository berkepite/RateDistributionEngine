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

public class BloombergRestSubscriber implements ISubscriber {
    private final BloombergRestConfig config;
    private final BloombergRateMapper rateMapper;
    private final RateFactory rateFactory;
    private final ICoordinator coordinator;
    private final Logger LOGGER = LogManager.getLogger(BloombergRestSubscriber.class);
    private final ThreadPoolTaskExecutor executorService;
    private String credentials;

    public BloombergRestSubscriber(final BloombergRestConfig config, BloombergRateMapper rateMapper, RateFactory rateFactory, Coordinator coordinator, @Qualifier("subscriberExecutor") ThreadPoolTaskExecutor executorService) {
        this.config = config;
        this.coordinator = coordinator;
        this.executorService = executorService;
        this.rateMapper = rateMapper;
        this.rateFactory = rateFactory;
    }

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();

            LOGGER.info("Subscriber stopped. ({})", config.getName());
        }, "shutdown-hook-" + config.getName()));

        String username_password = config.getUsername() + ":" + config.getPassword();
        credentials = "Basic " + Base64.getEncoder()
                .encodeToString(username_password.getBytes());
    }

    @Override
    public void connect() {
        HttpRequest req = createHealthRequest();
        HttpClient client = HttpClient.newHttpClient();

        int healthRequestRetryLimit = config.getHealthRequestRetryLimit();
        int requestInterval = config.getRequestInterval();

        while (healthRequestRetryLimit > 0) {

            try {
                HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    coordinator.onConnect(this);
                    break;
                } else {
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
                ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                        .withHttpRequestError(e, req)
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

        for (String endpoint : endpoints) {
            HttpRequest req = createRateRequest(endpoint);

            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

                if (res.statusCode() == 200) {
                    coordinator.onSubscribe(this);
                    coordinator.onRateAvailable(this, rateMapper.mapEndpointToRateEnum(endpoint));

                    executorService.execute(() -> subscribeToRate(endpoint, config));
                } else {
                    ConnectionStatus connectionStatus = ConnectionStatus.newBuilder()
                            .withHttpResponse(res, req)
                            .withMethod("subscribe")
                            .withSubscriber(this)
                            .withNotes("Response status code was different than 200")
                            .build();

                    coordinator.onConnectionError(this, connectionStatus);
                }


            } catch (IOException | InterruptedException e) {
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

    private void subscribeToRate(String endpoint, BloombergRestConfig config) {
        int retryLimit = config.getRequestRetryLimit();
        int requestInterval = config.getRequestInterval();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest req = createRateRequest(endpoint);

        while (retryLimit > 0) {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.warn("Thread is interrupted. ({})", Thread.currentThread().getName());
                break;
            }

            try {
                Thread.sleep(requestInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread is interrupted. ({}) {}", Thread.currentThread().getName(), e.getMessage());
                break;
            }

            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

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

    }

    @Override
    public void disConnect() {

    }

    @Override
    public ICoordinator getCoordinator() {
        return coordinator;
    }

    @Override
    public ISubscriberConfig getConfig() {
        return config;
    }

    private HttpRequest createRateRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl() + "/api/currencies/" + endpoint))
                .setHeader("Authorization", credentials)
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
    }

    private HttpRequest createHealthRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl() + "/api/health"))
                .setHeader("Authorization", credentials)
                .GET()
                .build();
    }
}
