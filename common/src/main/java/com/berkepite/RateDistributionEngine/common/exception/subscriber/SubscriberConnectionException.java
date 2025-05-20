package com.berkepite.RateDistributionEngine.common.exception.subscriber;

public class SubscriberConnectionException extends SubscriberException {
    public SubscriberConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriberConnectionException(String message) {
        super(message);
    }
}
