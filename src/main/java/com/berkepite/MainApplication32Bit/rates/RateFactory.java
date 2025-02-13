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

    public RateEntity createRate(SubscriberEnum type, String data) throws Exception {

        switch (type) {
            case CNN_TCP -> {
                RateEntity rateEntity = new RateEntity();
                cnnRateMapper.mapRate(data, rateEntity);
                return rateEntity;
            }
            case BLOOMBERG_REST -> {
                RateEntity rateEntity = new RateEntity();
                bloombergRateMapper.mapRate(data, rateEntity);
                return rateEntity;
            }
            default -> throw new IllegalArgumentException("Unknown subscriber type: " + type);
        }
    }
}
