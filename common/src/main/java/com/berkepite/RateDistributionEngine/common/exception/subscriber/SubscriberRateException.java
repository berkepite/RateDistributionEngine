package com.berkepite.RateDistributionEngine.common.exception.subscriber;

public class SubscriberRateException extends SubscriberException {
    public SubscriberRateException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriberRateException(String message) {
        super(message);
    }
}
