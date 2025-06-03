package com.berkepite.RateDistributionEngine.common.calculator;

import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorLoadingException;

import java.nio.file.Path;

public interface ICalculatorLoader {
    Path load(String path) throws CalculatorLoadingException;
}
