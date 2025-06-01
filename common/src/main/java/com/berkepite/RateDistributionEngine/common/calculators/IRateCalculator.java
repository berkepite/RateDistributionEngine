package com.berkepite.RateDistributionEngine.common.calculators;

import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.MeanRate;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;

public interface IRateCalculator {
    void init(String calculatorPath) throws CalculatorException;

    MeanRate calculateMeanRate(Double[] bids, Double[] asks) throws CalculatorException;

    CalculatedRate calculateForRawRateType(String type, Double usdmid, Double[] bids, Double[] asks) throws CalculatorException;

    CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks) throws CalculatorException;

    boolean hasAtLeastOnePercentDiff(RawRate incomingRate, MeanRate meanRate) throws CalculatorException;

    Double calculateUSDMID(Double[] bids, Double[] asks) throws CalculatorException;

    String getStrategy();

    String getPath();

}
