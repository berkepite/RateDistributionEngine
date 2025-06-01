package com.berkepite.RateDistributionEngine.common.calculators;

public interface ICalculatorFactory {
    IRateCalculator getCalculator() throws Exception;
}
