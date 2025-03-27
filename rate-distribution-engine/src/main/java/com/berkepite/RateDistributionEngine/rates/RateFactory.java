package com.berkepite.RateDistributionEngine.rates;

import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RateFactory {

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
