package com.berkepite.RateDistributionEngine.common.calculators;

import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorLoadingException;

import java.nio.file.Path;

public interface ICalculatorLoader {
    Path load(String path) throws CalculatorLoadingException;
}
