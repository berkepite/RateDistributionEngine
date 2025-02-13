package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class BloombergRateMapper {

    public List<String> mapRateEnumToEndpoints(List<RateEnum> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    public RateEntity mapRate(String data, RateEntity rate) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(data);

        RateEnum type = mapEndpointToRateEnum(json.get("name").asText());
        rate.setRate(type);
        rate.setAsk(json.get("ask").asDouble());
        rate.setBid(json.get("bid").asDouble());
        Instant timestamp = Instant.parse(json.get("timestamp").asText());
        rate.setTimestamp(timestamp);
        rate.setProvider(SubscriberEnum.BLOOMBERG_REST);

        return rate;
    }

    public String mapRateEnumToEndpoint(RateEnum rate) {
        String endpoint;
        endpoint = rate.toString().replace("_", "");

        return endpoint;
    }

    public RateEnum mapEndpointToRateEnum(String rateStr) {
        RateEnum rate;
        rate = RateEnum.valueOf(rateStr.substring(0, 3) + "_" + rateStr.substring(3));

        return rate;
    }
}
