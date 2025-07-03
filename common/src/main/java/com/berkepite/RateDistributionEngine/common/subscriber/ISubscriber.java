package com.berkepite.RateDistributionEngine.common.subscriber;

import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import org.apache.logging.log4j.Logger;

import java.util.List;

public interface ISubscriber {

    ICoordinator getCoordinator();

    ISubscriberConfig getConfig();

    Logger getLogger();

    void init() throws Exception;

    void connect() throws Exception;

    void disConnect();

    void subscribe(List<String> rates) throws Exception;

    void unSubscribe(List<String> rates);

}
