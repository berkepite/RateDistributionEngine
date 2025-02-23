package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.RawRateEnum;
import com.berkepite.MainApplication32Bit.subscribers.SubscriberBindingConfig;

import java.util.List;

public class CoordinatorConfig {
    private List<SubscriberBindingConfig> subscriberBindingConfigs;
    private List<RawRateEnum> rates;

    public List<RawRateEnum> getRates() {
        return rates;
    }

    public void setRates(List<RawRateEnum> rates) {
        this.rates = rates;
    }

    public List<SubscriberBindingConfig> getSubscriberBindingConfigs() {
        return subscriberBindingConfigs;
    }

    public void setSubscriberBindingConfigs(List<SubscriberBindingConfig> subscriberBindingConfigs) {
        this.subscriberBindingConfigs = subscriberBindingConfigs;
    }
}