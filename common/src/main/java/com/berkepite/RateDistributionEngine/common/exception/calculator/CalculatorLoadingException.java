package com.berkepite.RateDistributionEngine.common.exception.calculator;

public class CalculatorLoadingException extends CalculatorException {
    public CalculatorLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalculatorLoadingException(String message) {
        super(message);
    }
}
