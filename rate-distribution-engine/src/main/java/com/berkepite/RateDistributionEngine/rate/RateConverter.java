package com.berkepite.RateDistributionEngine.rate;

import com.berkepite.RateDistributionEngine.common.rate.IRateConverter;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link IRateConverter} that converts raw rate types to a calculated rate format.
 */
@Component
public class RateConverter implements IRateConverter {

    /**
     * Converts a raw rate type string to the calculated rate type format.
     * <p>
     * If the raw rate type is "USD_TRY", it returns it unchanged.
     * Otherwise, it takes the first 4 characters and appends "TRY" to form the calculated rate type.
     * </p>
     *
     * @param rawRateType the raw rate type string, e.g. "EUR_USD".
     * @return the calculated rate type string, e.g. "EUR_TRY".
     */
    public String convertFromRawToCalc(String rawRateType) {
        if (rawRateType.equals("USD_TRY")) {
            return rawRateType;
        } else {
            String significantHalf = rawRateType.substring(0, 4);
            return significantHalf.concat("TRY");
        }
    }
}
