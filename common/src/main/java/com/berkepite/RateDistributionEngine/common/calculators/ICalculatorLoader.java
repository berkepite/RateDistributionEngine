package com.berkepite.RateDistributionEngine.common.calculators;

import java.io.Reader;

public interface ICalculatorLoader {
    Reader load(String path) throws RuntimeException;
}
