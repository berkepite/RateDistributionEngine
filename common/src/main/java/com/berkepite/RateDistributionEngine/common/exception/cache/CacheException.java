package com.berkepite.RateDistributionEngine.common.exception.cache;

public class CacheException extends Exception {
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheException(String message) {
        super(message);
    }
}
