package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;

import java.util.List;

public class TCPSubscriber implements ISubscriber {
    private SubscriberConfig config;
    private ICoordinator coordinator;

    public TCPSubscriber() {
    }

    @Override
    public ICoordinator getCoordinator() {
        return coordinator;
    }

    @Override
    public void setCoordinator(ICoordinator coordinator) {
        this.coordinator = coordinator;
    }

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
    public void subscribe(List<RateEnum> rates) {

    }

    @Override
    public void unSubscribe(List<RateEnum> rates) {

    }

    @Override
    public IRate convertToRate() {
        return null;
    }
}