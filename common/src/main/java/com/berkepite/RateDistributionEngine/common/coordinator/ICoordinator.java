package com.berkepite.RateDistributionEngine.common.coordinator;

import com.berkepite.RateDistributionEngine.common.calculators.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriber;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;

import java.util.List;

public interface ICoordinator {
    void onConnect(ISubscriber subscriber);

    void onSubscribe(ISubscriber subscriber);

    void onUnSubscribe(ISubscriber subscriber);

    void onDisConnect(ISubscriber subscriber);

    void onRateAvailable(ISubscriber subscriber, String rate);

    void onRateUpdate(ISubscriber subscriber, RawRate rate);

    void onSubscriberError(ISubscriber subscriber, Exception e);

    void onRateError(ISubscriber subscriber, IRateCalculator calculator, Exception e);

    void onCalculatorError(IRateCalculator calculator, Exception e);

    List<ISubscriber> getSubscribers();

    ICoordinatorConfig getConfig();
}
