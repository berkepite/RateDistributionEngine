package com.berkepite.RateDistributionEngine.common.exception.subscriber;

public class SubscriberConnectionLostException extends SubscriberException {
    public SubscriberConnectionLostException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriberConnectionLostException(String message) {
        super(message);
    }
}
