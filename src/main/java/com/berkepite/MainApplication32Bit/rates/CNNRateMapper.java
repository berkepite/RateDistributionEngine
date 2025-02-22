package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberEnum;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CNNRateMapper {

    public RawRate mapRate(String data, RawRate rate) throws Exception {
        List<String> fields = Arrays.stream(data.split("\\|")).toList();

        List<String> nameField = Arrays.stream(fields.getFirst().split("=")).toList();
        List<String> bidField = Arrays.stream(fields.get(1).split("=")).toList();
        List<String> askField = Arrays.stream(fields.get(2).split("=")).toList();
        List<String> timestampField = Arrays.stream(fields.get(3).split("=")).toList();

        RateEnum type = mapEndpointToRateEnum(nameField.get(1));

        rate.setType(type.toString());
        rate.setAsk(Double.parseDouble(askField.get(1)));
        rate.setBid(Double.parseDouble(bidField.get(1)));
        Instant truncatedTimestamp = Instant.parse(timestampField.get(1)).truncatedTo(ChronoUnit.SECONDS);
        rate.setTimestamp(truncatedTimestamp);
        rate.setProvider(SubscriberEnum.CNN_TCP.toString());
        return rate;
    }

    public String mapRateEnumToEndpoint(RateEnum rate) {
        String endpoint;
        endpoint = rate.toString().replace("_", "");

        return endpoint;
    }

    public List<String> mapRateEnumToEndpoints(List<RateEnum> rates) {
        List<String> endpoints = new ArrayList<>();
        rates.forEach(r -> endpoints.add(mapRateEnumToEndpoint(r)));

        return endpoints;
    }

    public RateEnum mapEndpointToRateEnum(String rateStr) {
        RateEnum rate;
        rate = RateEnum.valueOf(rateStr.substring(0, 3) + "_" + rateStr.substring(3));

        return rate;
    }
}
