package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.subscribers.SubscriberBindingConfig;

import java.util.List;

public class CoordinatorConfig {
    private List<SubscriberBindingConfig> subscriberBindingConfigs;
    private List<RateEnum> rates;

    public List<RateEnum> getRates() {
        return rates;
    }

    public void setRates(List<RateEnum> rates) {
        this.rates = rates;
    }

    public List<SubscriberBindingConfig> getSubscriberBindingConfigs() {
        return subscriberBindingConfigs;
    }

    public void setSubscriberBindingConfigs(List<SubscriberBindingConfig> subscriberBindingConfigs) {
        this.subscriberBindingConfigs = subscriberBindingConfigs;
    }
}