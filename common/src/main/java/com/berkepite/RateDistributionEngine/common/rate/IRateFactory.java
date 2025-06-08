package com.berkepite.RateDistributionEngine.common.rate;

import java.time.Instant;

public interface IRateFactory {
    MeanRate createMeanRate(Double meanBid, Double meanAsk);

    RawRate createRawRate(String type, String provider, Double bid, Double ask, Instant timestamp);

    CalculatedRate createCalcRate(String type, Double bid, Double ask, Instant timestamp);
}
