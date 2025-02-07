package com.berkepite.MainApplication32Bit.subscribers;


import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;

import java.util.List;

public interface ISubscriber {

    ICoordinator getCoordinator();

    void setCoordinator(ICoordinator coordinator);

    SubscriberConfig getConfig();

    void setConfig(SubscriberConfig config);

    void connect();

    void disConnect();

    void subscribe(List<RateEnum> rates);

    void unSubscribe(List<RateEnum> rates);

    IRate convertToRate();

    List<RateEnum> getRatesToSubscribe();

}
