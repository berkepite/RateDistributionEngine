package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;

public interface IRateCalculator {
    RawRate calculateMeanRate(RawRate incomingRate, Double[] bids, Double[] asks);

    CalculatedRate calculateForRawRateType(String type, Double usdmid, Double[] bids, Double[] asks);

    CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks);

    boolean hasAtLeastOnePercentDiff(RawRate incomingRate, RawRate meanRate);

    Double calculateUSDMID(Double[] bids, Double[] asks);
}
