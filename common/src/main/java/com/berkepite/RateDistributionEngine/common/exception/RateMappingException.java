package com.berkepite.RateDistributionEngine.common.exception;

public class RateMappingException extends Exception {
    public RateMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RateMappingException(String message) {
        super(message);
    }
}
