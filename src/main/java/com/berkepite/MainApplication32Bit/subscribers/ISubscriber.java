package com.berkepite.MainApplication32Bit.subscribers;


import com.berkepite.MainApplication32Bit.rates.IRate;
import com.berkepite.MainApplication32Bit.rates.RateEnum;

public interface ISubscriber {

    SubscriberConfig getConfig();

    void setConfig(SubscriberConfig config);

    void connect();

    void disConnect();

    void subscribe(RateEnum rate);

    void unSubscribe(RateEnum rate);

    IRate convertToRate();

}
