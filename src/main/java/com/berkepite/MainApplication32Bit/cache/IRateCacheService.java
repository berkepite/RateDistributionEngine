package com.berkepite.MainApplication32Bit.cache;

import com.berkepite.MainApplication32Bit.rates.CalculatedRate;
import com.berkepite.MainApplication32Bit.rates.RawRate;

import java.util.List;

public interface IRateCacheService {
    CalculatedRate getCalcRate(CalculatedRate rate);

    RawRate getRawRate(RawRate rate);

    CalculatedRate saveCalcRate(CalculatedRate rate);

    Double saveUSDMID(Double value);

    Double getUSDMID();

    RawRate saveRawRate(RawRate rate);

    List<RawRate> getAllRawRatesForType(String type);
}
