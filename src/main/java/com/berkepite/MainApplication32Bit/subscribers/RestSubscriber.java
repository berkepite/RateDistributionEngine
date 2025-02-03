package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;

public class RestSubscriber implements ISubscriber {
    private SubscriberConfig config;

    @Override
    public SubscriberConfig getConfig() {
        return null;
    }

    @Override
    public void setConfig(SubscriberConfig config) {
        this.config = config;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disConnect() {

    }

    @Override
    public void subscribe(RateEnum rate) {

    }

    @Override
    public void unSubscribe(RateEnum rate) {

    }

    @Override
    public IRate convertToRate() {
        return null;
    }
}
