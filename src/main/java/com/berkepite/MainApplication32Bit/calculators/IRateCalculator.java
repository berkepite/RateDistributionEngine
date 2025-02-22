package com.berkepite.MainApplication32Bit.calculators;

import com.berkepite.MainApplication32Bit.rates.RawRate;

import java.util.List;

public interface IRateCalculator {
    RawRate calculateMean(RawRate rawRate, List<RawRate> otherPlatformRates);

    boolean hasAtLeastOnePercentDiff(RawRate rate1, RawRate rate2);

    Double calculateUSDMID(List<RawRate> ratesOfTypeUSDTRY);
}
