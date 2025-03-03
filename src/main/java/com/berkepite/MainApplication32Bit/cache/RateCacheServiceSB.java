package com.berkepite.MainApplication32Bit.cache;

import com.berkepite.MainApplication32Bit.rates.CalculatedRate;
import com.berkepite.MainApplication32Bit.rates.RawRate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

// WIP

@Service
@Profile({"debug"})
public class RateCacheServiceSB implements IRateCacheService {
    @Override
    @Cacheable(value = "calc_rates", key = "'rates:' + #rate.type.toString()")
    public CalculatedRate getCalcRate(CalculatedRate rate) {
        return null;
    }

    @Override
    @CachePut(value = "calc_rates", key = "'rates:' + #rate.type.toString()")
    public CalculatedRate saveCalcRate(CalculatedRate rate) {
        return rate;
    }

    @Override
    public Double saveUSDMID(Double value) {
        return 0.0;
    }

    @Override
    public Double getUSDMID() {
        return 0.0;
    }

    @Override
    @CachePut(value = "raw_rates", key = "'rates:' + #rate.provider.toString() + ':' + #rate.type.toString()")
    public RawRate saveRawRate(RawRate rate) {
        return rate;
    }

    @Override
    @Cacheable(value = "raw_rates", key = "'rates:' + #rate.provider.toString() + ':' + #rate.type.toString()")
    public RawRate getRawRate(RawRate rate) {
        return rate;
    }

    @Override
    public List<RawRate> getAllRawRatesForType(String type) {
        return List.of();
    }
}