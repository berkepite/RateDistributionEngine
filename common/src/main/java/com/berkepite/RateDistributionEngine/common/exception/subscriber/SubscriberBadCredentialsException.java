package com.berkepite.RateDistributionEngine.common.exception.subscriber;

public class SubscriberBadCredentialsException extends SubscriberConnectionException {
    public SubscriberBadCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriberBadCredentialsException(String message) {
        super(message);
    }
}
