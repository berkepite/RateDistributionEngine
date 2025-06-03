package com.berkepite.RateDistributionEngine.common.cache;

import com.berkepite.RateDistributionEngine.common.exception.cache.CacheException;
import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;

import java.util.List;

public interface IRateCacheService {
    CalculatedRate getCalcRate(CalculatedRate rate) throws CacheException;

    RawRate getRawRate(RawRate rate) throws CacheException;

    CalculatedRate saveCalcRate(CalculatedRate rate) throws CacheException;

    Double saveUSDMID(Double value) throws CacheException;

    Double getUSDMID() throws CacheException;

    RawRate saveRawRate(RawRate rate) throws CacheException;

    List<RawRate> getAllRawRatesForType(String type) throws CacheException;

    String getName();
}
