package com.berkepite.RateDistributionEngine.rates;

import org.springframework.stereotype.Component;

@Component
public class RateConverter {
    public String convertFromRawToCalc(String rawRateType) {
        if (rawRateType.equals("USD_TRY")) {
            return rawRateType;
        } else {
            String significantHalf = rawRateType.substring(0, 4);

            return significantHalf.concat("TRY");
        }
    }
}
