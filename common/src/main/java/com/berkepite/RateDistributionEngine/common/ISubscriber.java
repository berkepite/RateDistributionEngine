package com.berkepite.RateDistributionEngine.common;

import java.util.List;

public interface ISubscriber {

    ICoordinator getCoordinator();

    ISubscriberConfig getConfig();

    void init() throws Exception;

    void connect() throws Exception;

    void disConnect();

    void subscribe(List<String> rates) throws Exception;

    void unSubscribe(List<String> rates);

}
