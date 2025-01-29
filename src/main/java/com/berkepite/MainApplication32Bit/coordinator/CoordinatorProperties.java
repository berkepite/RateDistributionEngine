package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberModel;

import java.util.List;

public class CoordinatorProperties {

    private List<SubscriberModel> subscribers;

    public List<SubscriberModel> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<SubscriberModel> subscribers) {
        this.subscribers = subscribers;
    }
}
