package com.berkepite.RateDistributionEngine.CNNTCPSubscriber;

import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CNNRateMapper {

    public RawRate createRawRate(String data) throws Exception {
        RawRate rate = new RawRate();
        
        List<String> fields = Arrays.stream(data.split("\\|")).toList();

        List<String> nameField = Arrays.stream(fields.getFirst().split("=")).toList();
        List<String> bidField = Arrays.stream(fields.get(1).split("=")).toList();
        List<String> askField = Arrays.stream(fields.get(2).split("=")).toList();
        List<String> timestampField = Arrays.stream(fields.get(3).split("=")).toList();

        String type = mapEndpointToRateEnum(nameField.get(1));

        rate.setType(type);
        rate.setAsk(Double.parseDouble(askField.get(1)));
        rate.setBid(Double.parseDouble(bidField.get(1)));
        Instant truncatedTimestamp = Instant.parse(timestampField.get(1)).truncatedTo(ChronoUnit.SECONDS);
        rate.setTimestamp(truncatedTimestamp);
        rate.setProvider("CNN_TCP");
        return rate;
    }

    public String mapRateEnumToEndpoint(String rate) {
        String endpoint;
        endpoint = rate.replace("_", "");

        return endpoint;
    }

    public List<String> mapRateEnumToEndpoints(List<String> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    public String mapEndpointToRateEnum(String rateStr) {
        String rate;
        rate = rateStr.substring(0, 3) + "_" + rateStr.substring(3);

        return rate;
    }
}
