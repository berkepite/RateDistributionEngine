package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.rates.RateStatus;
import com.berkepite.MainApplication32Bit.subscribers.SubscriberConfig;

public interface ICoordinator {
    void onConnect(SubscriberConfig subscriberConfig, Boolean status);

    void onDisConnect(SubscriberConfig subscriberConfig);

    void onRateAvailable(SubscriberConfig subscriberConfig, RateEnum rate);

    void onRateUpdate(SubscriberConfig subscriberConfig, IRate rate);

    void onRateStatus(SubscriberConfig subscriberConfig, RateStatus rateStatus);

}
