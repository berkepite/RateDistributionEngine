package com.berkepite.MainApplication32Bit.rates;

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

    public BloombergRate mapRate(String data, BloombergRate rate) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(data);

        RateEnum type = mapEndpointToRateEnum(json.get("name").asText());
        rate.setType(type);
        rate.setAsk(json.get("ask").asDouble());
        rate.setBid(json.get("bid").asDouble());
        Instant timestamp = Instant.parse(json.get("timestamp").asText());
        rate.setTimeStamp(timestamp);


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
