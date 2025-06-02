package com.berkepite.RateDistributionEngine.RestSubscriber;

import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberRateException;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RateMapper {

    public RawRate createRawRate(String data) throws SubscriberRateException {
        RawRate rate = new RawRate();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode json = objectMapper.readTree(data);

            rate.setType(mapEndpointToRateEnum(json.get("name").asText()));
            rate.setAsk(json.get("ask").asDouble());
            rate.setBid(json.get("bid").asDouble());

            Instant truncatedTimestamp = Instant.parse(json.get("timestamp").asText()).truncatedTo(ChronoUnit.SECONDS);
            rate.setTimestamp(truncatedTimestamp);

            rate.setProvider("REST_PROVIDER");

        } catch (Exception e) {
            throw new SubscriberRateException("Could not parse Raw Rate data: %s".formatted(data), e.getCause());
        }

        return rate;
    }

    public List<String> mapRateEnumToEndpoints(List<String> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    public String mapRateEnumToEndpoint(String rate) {
        String endpoint;
        endpoint = rate.replace("_", "");

        return endpoint;
    }

    public String mapEndpointToRateEnum(String rateStr) throws Exception {
        String rate;
        rate = rateStr.substring(0, 3) + "_" + rateStr.substring(3);

        return rate;
    }
}
