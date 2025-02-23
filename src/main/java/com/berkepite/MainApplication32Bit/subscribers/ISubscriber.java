package com.berkepite.MainApplication32Bit.subscribers;


import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import com.berkepite.MainApplication32Bit.rates.RawRateEnum;

import java.util.List;

public interface ISubscriber {

    ICoordinator getCoordinator();

    ISubscriberConfig getConfig();

    void connect();

    void disConnect();

    void subscribe(List<RawRateEnum> rates);

    void unSubscribe(List<RawRateEnum> rates);

}
