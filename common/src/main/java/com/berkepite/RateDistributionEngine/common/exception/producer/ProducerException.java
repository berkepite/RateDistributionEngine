package com.berkepite.RateDistributionEngine.common.exception.producer;

public class ProducerException extends Exception {
    public ProducerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProducerException(String message) {
        super(message);
    }
}
