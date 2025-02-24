package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RateFactory {
    private final CNNRateMapper cnnRateMapper;
    private final BloombergRateMapper bloombergRateMapper;

    @Autowired
    public RateFactory(CNNRateMapper cnnRateMapper, BloombergRateMapper bloombergRateMapper) {
        this.cnnRateMapper = cnnRateMapper;
        this.bloombergRateMapper = bloombergRateMapper;
    }

    public RawRate createRateFromData(SubscriberEnum type, String data) throws Exception {

        switch (type) {
            case CNN_TCP -> {
                RawRate rawRate = new RawRate();
                cnnRateMapper.mapRate(data, rawRate);
                return rawRate;
            }
            case BLOOMBERG_REST -> {
                RawRate rawRate = new RawRate();
                bloombergRateMapper.mapRate(data, rawRate);
                return rawRate;
            }
            default -> throw new IllegalArgumentException("Unknown subscriber type: " + type);
        }
    }

    public RawRate createRawRate(String type, String provider, Double bid, Double ask, Instant timestamp) {
        RawRate rawRate = new RawRate();
        rawRate.setType(type);
        rawRate.setProvider(provider);
        rawRate.setBid(bid);
        rawRate.setAsk(ask);
        rawRate.setTimestamp(timestamp);

        return rawRate;
    }

    public CalculatedRate createCalcRate(String type, Double bid, Double ask, Instant timestamp) {
        CalculatedRate calculatedRate = new CalculatedRate();
        calculatedRate.setType(type);
        calculatedRate.setBid(bid);
        calculatedRate.setAsk(ask);
        calculatedRate.setTimestamp(timestamp);

        return calculatedRate;
    }


}
