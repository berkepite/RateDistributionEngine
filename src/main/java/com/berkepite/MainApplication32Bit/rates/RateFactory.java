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


}
