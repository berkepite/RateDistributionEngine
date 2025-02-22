package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.RateEnum;
import com.berkepite.MainApplication32Bit.subscribers.SubscriberBindingConfig;

import java.util.List;

public class CoordinatorConfig {
    private List<SubscriberBindingConfig> subscriberBindingConfigs;
    private List<RateEnum> rates;
    private String rateCalculationStrategy;
    private String rateCalculationSourcePath;

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

    public String getRateCalculationStrategy() {
        return rateCalculationStrategy;
    }

    public void setRateCalculationStrategy(String rateCalculationStrategy) {
        this.rateCalculationStrategy = rateCalculationStrategy;
    }

    public String getRateCalculationSourcePath() {
        return rateCalculationSourcePath;
    }

    public void setRateCalculationSourcePath(String rateCalculationSourcePath) {
        this.rateCalculationSourcePath = rateCalculationSourcePath;
    }
}