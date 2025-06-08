package com.berkepite.RateDistributionEngine.common.exception.calculator;

public class CalculatorException extends Exception {
    public CalculatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalculatorException(String message) {
        super(message);
    }
}
