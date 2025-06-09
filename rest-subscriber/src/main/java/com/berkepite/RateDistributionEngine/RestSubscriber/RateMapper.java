package com.berkepite.RateDistributionEngine.RestSubscriber;

import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberRateException;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * RateMapper is responsible for converting between REST API response data and internal rate representations.
 * It parses JSON rate data into RawRate objects and maps between rate enums and REST API endpoint strings.
 */
public class RateMapper {

    /**
     * Parses a JSON string representing a rate into a RawRate object.
     *
     * @param data JSON string containing rate information.
     * @return RawRate object constructed from the JSON data.
     * @throws SubscriberRateException if the JSON parsing fails or data is invalid.
     */
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

    /**
     * Maps a list of rate enum strings to their corresponding REST API endpoint strings.
     *
     * @param rates List of rate enum strings (e.g., "USD_EUR").
     * @return List of endpoint strings corresponding to the given rate enums (e.g., "USDEUR").
     */
    public List<String> mapRateEnumToEndpoints(List<String> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    /**
     * Maps a single rate enum string to its corresponding REST API endpoint string.
     *
     * @param rate Rate enum string (e.g., "USD_EUR").
     * @return Endpoint string corresponding to the rate (e.g., "USDEUR").
     */
    public String mapRateEnumToEndpoint(String rate) {
        String endpoint;
        endpoint = rate.replace("_", "");

        return endpoint;
    }

    /**
     * Maps a REST API endpoint string back to the internal rate enum format.
     *
     * @param rateStr Endpoint string from REST API (e.g., "USDEUR").
     * @return Rate enum string with underscore (e.g., "USD_EUR").
     * @throws Exception if the input string length is insufficient to perform mapping.
     */
    public String mapEndpointToRateEnum(String rateStr) throws Exception {
        String rate;
        rate = rateStr.substring(0, 3) + "_" + rateStr.substring(3);

        return rate;
    }
}
