package com.berkepite.RateDistributionEngine.common.exception.subscriber;

public class SubscriberClassNotFoundException extends SubscriberLoadingException {
    public SubscriberClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriberClassNotFoundException(String message) {
        super(message);
    }
}
