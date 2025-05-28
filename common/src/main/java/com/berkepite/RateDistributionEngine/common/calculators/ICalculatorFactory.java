package com.berkepite.RateDistributionEngine.common.calculators;

public interface ICalculatorFactory {
    IRateCalculator getCalculator(CalculatorEnum strategy, String path) throws Exception;
}
