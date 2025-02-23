package com.berkepite.MainApplication32Bit.rates;

import java.util.List;

public interface IRateCacheService {
    CalculatedRate getRate(CalculatedRate rate);

    RawRate getRawRate(RawRate rate);

    CalculatedRate saveRate(CalculatedRate rate);

    Double saveUSDMID(Double value);

    Double getUSDMID();

    RawRate saveRawRate(RawRate rate);

    List<RawRate> getAllRawRatesForType(String type);
}
