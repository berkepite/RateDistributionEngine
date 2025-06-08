package com.berkepite.RateDistributionEngine.common.calculator;

import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.MeanRate;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.apache.logging.log4j.Logger;

public interface IRateCalculator {
    void init(String calculatorPath) throws CalculatorException;

    MeanRate calculateMeanRate(Double[] bids, Double[] asks) throws CalculatorException;

    CalculatedRate calculateForRawRateType(String type, Double usdmid, Double[] bids, Double[] asks) throws CalculatorException;

    CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks) throws CalculatorException;

    boolean hasAtLeastOnePercentDiff(RawRate incomingRate, MeanRate meanRate) throws CalculatorException;

    Double calculateUSDMID(Double[] bids, Double[] asks) throws CalculatorException;

    String getStrategy();

    String getPath();

    Logger getLogger();
}
