package com.berkepite.RateDistributionEngine.common.calculator;

import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;

public interface ICalculatorFactory {
    IRateCalculator getCalculator() throws CalculatorException;
}
