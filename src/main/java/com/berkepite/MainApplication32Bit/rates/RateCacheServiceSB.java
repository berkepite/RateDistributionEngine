package com.berkepite.MainApplication32Bit.rates;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RateCacheServiceSB implements IRateCacheService {
    @Override
    @Cacheable(value = "rates", key = "'rates:' + #rate.provider.toString() + ':' + #rate.type.toString()")
    public RawRate getRate(RawRate rate) {
        return rate;
    }

    @Override
    @Cacheable(value = "raw_rates", key = "'rates:' + #rate.provider.toString() + ':' + #rate.type.toString()")
    public RawRate getRawRate(RawRate rate) {
        return rate;
    }

    @Override
    @CachePut(value = "rates", key = "'rates:' + #rate.provider.toString() + ':' + #rate.type.toString()")
    public RawRate saveRate(RawRate rate) {
        return rate;
    }

    @Override
    @CachePut(value = "raw_rates", key = "'rates:' + #rate.provider.toString() + ':' + #rate.type.toString()")
    public RawRate saveRawRate(RawRate rate) {
        return rate;
    }
}