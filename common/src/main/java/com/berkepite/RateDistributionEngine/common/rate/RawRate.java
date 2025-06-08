package com.berkepite.RateDistributionEngine.common.rate;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

public class RawRate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String provider;
    private String type;
    private double bid;
    private double ask;
    private Instant timestamp;

    @Override
    public String toString() {
        return "RawRate [type=" + type + "," +
                " provider=" + provider + "," +
                " bid=" + bid + "," +
                " ask=" + ask + "," +
                " timestamp=" + timestamp + "]";
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String currencyPair) {
        this.type = currencyPair;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

}
