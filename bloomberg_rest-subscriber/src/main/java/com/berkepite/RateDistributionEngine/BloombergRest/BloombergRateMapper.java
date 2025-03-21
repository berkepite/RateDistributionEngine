package com.berkepite.RateDistributionEngine.BloombergRest;

import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BloombergRateMapper {

    public List<String> mapRateEnumToEndpoints(List<String> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    public RawRate createRawRate(String data) throws JsonProcessingException {
        RawRate rate = new RawRate();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(data);

        rate.setType(json.get("name").asText());
        rate.setAsk(json.get("ask").asDouble());
        rate.setBid(json.get("bid").asDouble());
        Instant truncatedTimestamp = Instant.parse(json.get("timestamp").asText());
        rate.setTimestamp(truncatedTimestamp);
        rate.setProvider("BLOOMBERG_REST");

        return rate;
    }

    public String mapRateEnumToEndpoint(String rate) {
        String endpoint;
        endpoint = rate.replace("_", "");

        return endpoint;
    }

    public String mapEndpointToRateEnum(String rateStr) {
        String rate;
        rate = rateStr.substring(0, 3) + "_" + rateStr.substring(3);

        return rate;
    }
}
