package com.berkepite.RateDistributionEngine.coordinator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.coordinator")
public class CoordinatorConfig {

    private List<SubscriberBindingConfig> subscriberBindings;
    private List<String> rates;

    public List<String> getRates() {
        return rates;
    }

    public List<SubscriberBindingConfig> getSubscriberBindings() {
        return subscriberBindings;
    }

    public void setRates(List<String> rates) {
        this.rates = rates;
    }

    public void setSubscriberBindings(List<SubscriberBindingConfig> subscriberBindings) {
        this.subscriberBindings = subscriberBindings;
    }

    @Override
    public String toString() {
        return "CoordinatorConfig{" +
                "subscribers=" + subscriberBindings +
                ", rates=" + rates +
                '}';
    }
}