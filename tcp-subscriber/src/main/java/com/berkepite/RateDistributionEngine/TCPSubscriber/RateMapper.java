package com.berkepite.RateDistributionEngine.TCPSubscriber;

import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberRateException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberRateMappingException;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RateMapper is responsible for converting raw string data received from the TCP subscriber
 * into {@link RawRate} objects and mapping between rate enums and endpoint strings.
 */
public class RateMapper {

    /**
     * Parses a raw rate data string and creates a {@link RawRate} object.
     *
     * @param data raw rate data in the format "name=XXX|bid=XXX|ask=XXX|timestamp=XXX"
     * @return the parsed {@link RawRate}
     * @throws SubscriberRateException if the data cannot be parsed correctly
     */
    public RawRate createRawRate(String data) throws SubscriberRateException {
        RawRate rate = new RawRate();

        List<String> fields = Arrays.stream(data.split("\\|")).toList();

        List<String> nameField = Arrays.stream(fields.getFirst().split("=")).toList();
        List<String> bidField = Arrays.stream(fields.get(1).split("=")).toList();
        List<String> askField = Arrays.stream(fields.get(2).split("=")).toList();
        List<String> timestampField = Arrays.stream(fields.get(3).split("=")).toList();

        try {
            String type = mapEndpointToRateEnum(nameField.get(1));

            rate.setType(type);
            rate.setAsk(Double.parseDouble(askField.get(1)));
            rate.setBid(Double.parseDouble(bidField.get(1)));
            Instant truncatedTimestamp = Instant.parse(timestampField.get(1)).truncatedTo(ChronoUnit.SECONDS);
            rate.setTimestamp(truncatedTimestamp);
            rate.setProvider("TCP_PROVIDER");
        } catch (Exception e) {
            throw new SubscriberRateMappingException("Could not parse Raw Rate data: %s".formatted(data), e.getCause());
        }
        return rate;
    }

    /**
     * Converts a rate enum string (e.g. "EUR_USD") to an endpoint string by removing underscores (e.g. "EURUSD").
     *
     * @param rate the rate enum string
     * @return the corresponding endpoint string
     */
    public String mapRateEnumToEndpoint(String rate) {
        String endpoint;
        endpoint = rate.replace("_", "");

        return endpoint;
    }

    /**
     * Converts a list of rate enum strings to their corresponding endpoint strings.
     *
     * @param rates list of rate enum strings
     * @return list of endpoint strings
     */
    public List<String> mapRateEnumToEndpoints(List<String> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    /**
     * Converts an endpoint string (e.g. "EURUSD") back to a rate enum string by inserting an underscore (e.g. "EUR_USD").
     *
     * @param rateStr the endpoint string
     * @return the corresponding rate enum string
     * @throws Exception if the conversion fails
     */
    public String mapEndpointToRateEnum(String rateStr) throws Exception {
        String rate;
        rate = rateStr.substring(0, 3) + "_" + rateStr.substring(3);

        return rate;
    }
}
