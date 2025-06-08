package com.berkepite.RateDistributionEngine.common.exception.subscriber;

public class SubscriberException extends Exception {
    public SubscriberException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriberException(String message) {
        super(message);
    }
}
