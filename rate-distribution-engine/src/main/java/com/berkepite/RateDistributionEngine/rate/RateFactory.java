package com.berkepite.RateDistributionEngine.rate;

import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.IRateFactory;
import com.berkepite.RateDistributionEngine.common.rate.MeanRate;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Factory class to create instances of rate objects such as MeanRate, RawRate, and CalculatedRate.
 */
@Component
public class RateFactory implements IRateFactory {

    /**
     * Creates a new {@link MeanRate} instance with the given mean bid and ask values.
     *
     * @param meanBid the mean bid value
     * @param meanAsk the mean ask value
     * @return a {@link MeanRate} object with the specified bid and ask
     */
    public MeanRate createMeanRate(Double meanBid, Double meanAsk) {
        MeanRate meanRate = new MeanRate();
        meanRate.setMeanBid(meanBid);
        meanRate.setMeanAsk(meanAsk);
        return meanRate;
    }

    /**
     * Creates a new {@link RawRate} instance with the specified parameters.
     *
     * @param type      the type of the rate (e.g., "USD_TRY")
     * @param provider  the rate provider name
     * @param bid       the bid price
     * @param ask       the ask price
     * @param timestamp the timestamp of the rate, can be null
     * @return a {@link RawRate} object initialized with the provided values
     */
    public RawRate createRawRate(String type, String provider, Double bid, Double ask, Instant timestamp) {
        RawRate rawRate = new RawRate();
        rawRate.setType(type);
        rawRate.setProvider(provider);
        rawRate.setBid(bid);
        rawRate.setAsk(ask);
        rawRate.setTimestamp(timestamp);
        return rawRate;
    }

    /**
     * Creates a new {@link CalculatedRate} instance with the specified parameters.
     *
     * @param type      the type of the calculated rate (e.g., "EUR_TRY")
     * @param bid       the bid price
     * @param ask       the ask price
     * @param timestamp the timestamp of the calculated rate, can be null
     * @return a {@link CalculatedRate} object initialized with the provided values
     */
    public CalculatedRate createCalcRate(String type, Double bid, Double ask, Instant timestamp) {
        CalculatedRate calculatedRate = new CalculatedRate();
        calculatedRate.setType(type);
        calculatedRate.setBid(bid);
        calculatedRate.setAsk(ask);
        calculatedRate.setTimestamp(timestamp);
        return calculatedRate;
    }
}
