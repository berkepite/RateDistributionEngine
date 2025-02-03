package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.rates.RateEnum;

import java.util.ArrayList;
import java.util.List;

public class CoordinatorConfig {
    private List<RateEnum> rates;

    public CoordinatorConfig() {
        rates = new ArrayList<>();
    }

    public List<RateEnum> getRates() {
        return rates;
    }

    public void addRate(final RateEnum rate) {
        rates.add(rate);
    }

    public void setRates(List<RateEnum> rates) {
        this.rates = rates;
    }
}