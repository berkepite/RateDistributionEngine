package com.berkepite.RateDistributionEngine.common.rate;

public class MeanRate {
    private double meanBid;
    private double meanAsk;

    public double getMeanBid() {
        return meanBid;
    }

    public void setMeanBid(double meanBid) {
        this.meanBid = meanBid;
    }

    public double getMeanAsk() {
        return meanAsk;
    }

    public void setMeanAsk(double meanAsk) {
        this.meanAsk = meanAsk;
    }

    @Override
    public String toString() {
        return "MeanRate{" +
                "meanBid=" + meanBid +
                ", meanAsk=" + meanAsk +
                '}';
    }
}
