package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RateFactory {
    private final CNNRateMapper cnnRateMapper;
    private final BloombergRateMapper bloombergRateMapper;

    @Autowired
    public RateFactory(CNNRateMapper cnnRateMapper, BloombergRateMapper bloombergRateMapper) {
        this.cnnRateMapper = cnnRateMapper;
        this.bloombergRateMapper = bloombergRateMapper;
    }

    public IRate createRate(SubscriberEnum type, String data) throws Exception {

        switch (type) {
            case CNN_TCP -> {
                CNNRate rate = new CNNRate();
                cnnRateMapper.mapRate(data, rate);
                return rate;
            }
            case BLOOMBERG_REST -> {
                BloombergRate rate = new BloombergRate();
                bloombergRateMapper.mapRate(data, rate);
                return rate;
            }
            default -> throw new IllegalArgumentException("Unknown subscriber type: " + type);
        }
    }
}
