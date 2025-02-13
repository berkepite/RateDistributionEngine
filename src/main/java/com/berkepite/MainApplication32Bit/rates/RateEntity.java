package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberEnum;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "rates")
public class RateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private SubscriberEnum provider;
    private RateEnum rate;
    private double bid;
    private double ask;
    @Column(columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant timestamp;

    @Override
    public String toString() {
        return "Rate [type=" + rate + "," +
                " provider=" + provider + "," +
                " bid=" + bid + "," +
                " ask=" + ask + "," +
                " timestamp=" + timestamp.toString() + "]";
    }

    public Long getId() {
        return id;
    }

    public SubscriberEnum getProvider() {
        return provider;
    }

    public void setProvider(SubscriberEnum provider) {
        this.provider = provider;
    }

    public RateEnum getRate() {
        return rate;
    }

    public void setRate(RateEnum currencyPair) {
        this.rate = currencyPair;
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
