package com.berkepite.MainApplication32Bit.rates;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "rates")
public class RateEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;
    private String rate;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String currencyPair) {
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
