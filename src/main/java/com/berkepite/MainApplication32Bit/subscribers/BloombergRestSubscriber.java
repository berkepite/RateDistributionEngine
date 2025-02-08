package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.status.ConnectionStatus;
import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.BloombergRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.status.RateStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;

public class BloombergRestSubscriber implements ISubscriber {
    private SubscriberConfig config;
    private ICoordinator coordinator;
    private final Logger LOGGER = LogManager.getLogger(BloombergRestSubscriber.class);
    private List<RateEnum> ratesToSubscribe;
    private String credentials;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void connect() {
        String username_password = config.getUsername() + ":" + config.getPassword();
        credentials = "Basic " + Base64.getEncoder()
                .encodeToString(username_password.getBytes());

        HttpRequest req = createHealthRequest();
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            coordinator.onConnect(this, new ConnectionStatus(response, req));
        } catch (IOException | InterruptedException e) {
            coordinator.onConnectionError(this, new ConnectionStatus(e, req));

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void disConnect() {

    }

    @Override
    public void subscribe(List<RateEnum> rates) {
        ratesToSubscribe = new ArrayList<>(rates);
        ratesToSubscribe.addAll(config.getIncludeRates());
        ratesToSubscribe.removeAll(config.getExcludeRates());

        LOGGER.info("{} subscribing to {} ", config.getName(), ratesToSubscribe);
        List<String> endpoints = mapRateEnumToEndpoints(ratesToSubscribe);

        HttpClient client = HttpClient.newHttpClient();

        for (String endpoint : endpoints) {
            HttpRequest req = createRateRequest(endpoint);

            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

                coordinator.onSubscribe(this, new ConnectionStatus(res, req));
                coordinator.onRateAvailable(this, mapEndpointToRateEnum(endpoint));

                executorService.execute(() -> subscribeToRate(req, config));
            } catch (IOException | InterruptedException e) {
                coordinator.onConnectionError(this, new ConnectionStatus(e, req));

                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    private void subscribeToRate(HttpRequest req, SubscriberConfig config) {
        int retryLimit = Integer.parseInt(config.getRequestRetryLimit());
        int requestInterval = Integer.parseInt(config.getRequestInterval());

        HttpClient client = HttpClient.newHttpClient();

        while (retryLimit > 0) {
            try {
                Thread.sleep(requestInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                retryLimit--;
                continue;
            }

            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                JsonNode json = mapResponseToJson(res);
                BloombergRate rate = mapJsonToRate(json);

                coordinator.onRateUpdate(this, rate);

            } catch (IOException | InterruptedException e) {
                coordinator.onRateError(this, new RateStatus(e, req));

                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    retryLimit--;
                    continue;
                }

                retryLimit--;
            }
        }
    }

    @Override
    public void unSubscribe(List<RateEnum> rates) {

    }

    @Override
    public IRate convertToRate() {
        return null;
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
    public SubscriberConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(SubscriberConfig config) {
        this.config = config;
    }

    @Override
    public List<RateEnum> getRatesToSubscribe() {
        return ratesToSubscribe;
    }

    private HttpRequest createRateRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl() + "/api/currencies/" + endpoint))
                .setHeader("Authorization", credentials)
                .GET()
                .build();
    }

    private HttpRequest createHealthRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl() + "/api/health"))
                .setHeader("Authorization", credentials)
                .GET()
                .build();
    }

    private List<String> mapRateEnumToEndpoints(List<RateEnum> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    private String mapRateEnumToEndpoint(RateEnum rate) {
        String endpoint;
        endpoint = rate.toString().replace("_", "");

        return endpoint;
    }

    private RateEnum mapEndpointToRateEnum(String rateStr) {
        RateEnum rate;
        rate = RateEnum.valueOf(rateStr.substring(0, 3) + "_" + rateStr.substring(3));

        return rate;
    }

    private JsonNode mapResponseToJson(HttpResponse<String> response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(response.body());
    }

    private JsonNode mapStringToJson(String str) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(str);
    }

    private BloombergRate mapJsonToRate(JsonNode node) throws JsonProcessingException {
        BloombergRate rate = new BloombergRate();

        RateEnum type = mapEndpointToRateEnum(node.get("name").asText());
        rate.setType(type);
        rate.setAsk(node.get("ask").asDouble());
        rate.setBid(node.get("bid").asDouble());
        Instant timestamp = Instant.parse(node.get("timestamp").asText());
        rate.setTimeStamp(timestamp);

        return rate;
    }
}
