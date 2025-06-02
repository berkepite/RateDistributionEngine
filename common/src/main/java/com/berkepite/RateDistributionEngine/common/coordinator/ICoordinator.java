package com.berkepite.RateDistributionEngine.common.coordinator;

import com.berkepite.RateDistributionEngine.common.calculators.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberException;
import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriber;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;

import java.util.List;

public interface ICoordinator {
    void onConnect(ISubscriber subscriber);

    void onSubscribe(ISubscriber subscriber);

    void onUnSubscribe(ISubscriber subscriber, List<String> rates);

    void onDisConnect(ISubscriber subscriber);

    void onRateAvailable(ISubscriber subscriber, String rate);

    void onRateUpdate(ISubscriber subscriber, RawRate rate);

    void onSubscriberError(ISubscriber subscriber, SubscriberException e);

    void onCalculatorError(IRateCalculator calculator, CalculatorException e);

    String connect(String subscriberName);

    String disconnect(String subscriberName);

    String subscribe(String subscriberName, List<String> rates);

    String unSubscribe(String subscriberName, List<String> rates);

    List<ISubscriber> getSubscribers();

    ICoordinatorConfig getConfig();
}
