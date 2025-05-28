package com.berkepite.RateDistributionEngine.common.coordinator;

import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriber;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.common.status.ConnectionStatus;

import java.util.List;

public interface ICoordinator {
    void onConnect(ISubscriber subscriber);

    void onSubscribe(ISubscriber subscriber);

    void onUnSubscribe(ISubscriber subscriber);

    void onDisConnect(ISubscriber subscriber);

    void onRateAvailable(ISubscriber subscriber, String rate);

    void onRateUpdate(ISubscriber subscriber, RawRate rate);

    void onConnectionError(ISubscriber subscriber, ConnectionStatus status);

    List<ISubscriber> getSubscribers();

    ICoordinatorConfig getConfig();
}
