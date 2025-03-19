package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;

import java.util.List;

public interface IRateCalculator {
    RawRate calculateMeansOfRawRates(RawRate incomingRate, Double[] bids, Double[] asks);

    CalculatedRate calculateForType(String type, Double usdmid, Double[] bids, Double[] asks);

    CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks);

    boolean hasAtLeastOnePercentDiff(Double bid1, Double ask1, Double bid2, Double ask2);

    Double calculateMean(List<RawRate> rawRates);

    Double calculateUSDMID(Double[] bids, Double[] asks);

}
