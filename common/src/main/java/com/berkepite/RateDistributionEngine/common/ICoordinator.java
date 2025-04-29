package com.berkepite.RateDistributionEngine.common;

import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.common.status.ConnectionStatus;

public interface ICoordinator {
    void onConnect(ISubscriber subscriber);

    void onSubscribe(ISubscriber subscriber);

    void onUnSubscribe(ISubscriber subscriber);

    void onDisConnect(ISubscriber subscriber);

    void onRateAvailable(ISubscriber subscriber, String rate);

    void onRateUpdate(ISubscriber subscriber, RawRate rate);

    void onConnectionError(ISubscriber subscriber, ConnectionStatus status);
}
