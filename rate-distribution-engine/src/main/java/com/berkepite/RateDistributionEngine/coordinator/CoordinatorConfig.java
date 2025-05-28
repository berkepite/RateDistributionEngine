package com.berkepite.RateDistributionEngine.coordinator;

import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinatorConfig;
import com.berkepite.RateDistributionEngine.common.coordinator.ISubscriberBindingConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "app.coordinator")
public class CoordinatorConfig implements ICoordinatorConfig {

    private List<SubscriberBindingConfig> subscriberBindings;

    public void setSubscriberBindings(List<SubscriberBindingConfig> subscriberBindings) {
        this.subscriberBindings = subscriberBindings;
    }

    @Override
    public List<ISubscriberBindingConfig> getSubscriberBindings() {
        return subscriberBindings.stream()
                .map(sb -> (ISubscriberBindingConfig) sb)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "CoordinatorConfig{" +
                "subscribers=" + subscriberBindings +
                '}';
    }
}