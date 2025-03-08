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

    public List<String> mapRateEnumToEndpoints(List<RawRateEnum> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    public RawRate mapRate(String data, RawRate rate) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(data);

        RawRateEnum type = mapEndpointToRateEnum(json.get("name").asText());
        rate.setType(type.toString());
        rate.setAsk(json.get("ask").asDouble());
        rate.setBid(json.get("bid").asDouble());
        Instant truncatedTimestamp = Instant.parse(json.get("timestamp").asText());
        rate.setTimestamp(truncatedTimestamp);
        rate.setProvider(SubscriberEnum.BLOOMBERG_REST.toString());

        return rate;
    }

    public String mapRateEnumToEndpoint(RawRateEnum rate) {
        String endpoint;
        endpoint = rate.toString().replace("_", "");

        return endpoint;
    }

    public RawRateEnum mapEndpointToRateEnum(String rateStr) {
        RawRateEnum rate;
        rate = RawRateEnum.valueOf(rateStr.substring(0, 3) + "_" + rateStr.substring(3));

        return rate;
    }
}
