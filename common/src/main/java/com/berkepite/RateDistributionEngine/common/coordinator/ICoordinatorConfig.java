package com.berkepite.RateDistributionEngine.common.coordinator;

import java.util.List;

public interface ICoordinatorConfig {
    List<ISubscriberBindingConfig> getSubscriberBindings();
}
