package com.berkepite.RateDistributionEngine.rate;

import com.berkepite.RateDistributionEngine.common.rate.IRateConverter;
import org.springframework.stereotype.Component;

@Component
public class RateConverter implements IRateConverter {
    public String convertFromRawToCalc(String rawRateType) {
        if (rawRateType.equals("USD_TRY")) {
            return rawRateType;
        } else {
            String significantHalf = rawRateType.substring(0, 4);

            return significantHalf.concat("TRY");
        }
    }
}
