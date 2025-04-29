package com.berkepite.RateDistributionEngine.common.exception.subscriber;

public class SubscriberProperConstructorNotFoundException extends SubscriberLoadingException {
    public SubscriberProperConstructorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriberProperConstructorNotFoundException(String message) {
        super(message);
    }
}
