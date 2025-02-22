package com.berkepite.MainApplication32Bit.rates;

public interface IRateCacheService {
    RawRate getRate(RawRate rate);

    RawRate getRawRate(RawRate rate);

    RawRate saveRate(RawRate rate);

    RawRate saveRawRate(RawRate rate);
}
