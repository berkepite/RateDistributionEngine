package com.berkepite.RateDistributionEngine.common;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}