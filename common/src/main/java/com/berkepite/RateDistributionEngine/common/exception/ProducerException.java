package com.berkepite.RateDistributionEngine.common.exception;

public class ProducerException extends Exception {
    public ProducerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProducerException(String message) {
        super(message);
    }
}
