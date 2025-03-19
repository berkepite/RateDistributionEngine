package com.berkepite.RateDistributionEngine.RestSubscriberTest;

import com.berkepite.RateDistributionEngine.BloombergRestSubscriber.BloombergRestSubscriber;

public class MainTest {
    public static void main(String[] args) {
        try {
            BloombergRestSubscriber subscriber = new BloombergRestSubscriber(null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
