package com.berkepite.MainApplication32Bit.rates;

import java.time.Instant;

public class Rate implements IRate {
    private RateEnum rateType;
    private Double bid;
    private Double ask;
    private Instant timeStamp;

    @Override
    public RateEnum getType() {
        return rateType;
    }

    @Override
    public void setType(RateEnum type) {
        rateType = type;
    }

    @Override
    public Double getBid() {
        return bid;
    }

    @Override
    public Double getAsk() {
        return ask;
    }

    @Override
    public Instant getTimeStamp() {
        return timeStamp;
    }

    @Override
    public void setBid(Double bid) {
        this.bid = bid;
    }

    @Override
    public void setAsk(Double ask) {
        this.ask = ask;
    }

    @Override
    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }
}
