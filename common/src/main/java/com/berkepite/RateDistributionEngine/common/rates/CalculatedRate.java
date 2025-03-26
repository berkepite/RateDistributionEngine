package com.berkepite.RateDistributionEngine.common.rates;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

public class CalculatedRate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String type;
    private double bid;
    private double ask;
    private Instant timestamp;

    @Override
    public String toString() {
        return "CalculatedRate [type=" + type + "," +
                " bid=" + bid + "," +
                " ask=" + ask + "," +
                " timestamp=" + timestamp + "]";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
