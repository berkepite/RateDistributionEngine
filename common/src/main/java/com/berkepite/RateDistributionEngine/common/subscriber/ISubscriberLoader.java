package com.berkepite.RateDistributionEngine.common.subscriber;

import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import com.berkepite.RateDistributionEngine.common.coordinator.ISubscriberBindingConfig;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberLoadingException;

public interface ISubscriberLoader {
    ISubscriber load(ISubscriberBindingConfig bindingConfig, ICoordinator coordinator) throws SubscriberLoadingException;
}
