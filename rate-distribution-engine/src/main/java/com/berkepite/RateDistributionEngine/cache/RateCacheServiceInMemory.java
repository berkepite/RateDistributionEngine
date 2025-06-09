package com.berkepite.RateDistributionEngine.cache;

import com.berkepite.RateDistributionEngine.common.cache.IRateCacheService;
import com.berkepite.RateDistributionEngine.common.exception.cache.CacheException;
import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link IRateCacheService} for use in testing environments.
 * <p>
 * This service stores rates and USD mid values in local HashMaps, providing
 * fast and lightweight caching without external dependencies.
 * </p>
 * <p>
 * It is active only under the {@code test} Spring profile.
 * </p>
 * <p>
 * Note: This implementation is not thread-safe and should only be used for
 * testing or development scenarios where concurrent access is not expected.
 * </p>
 */
@Service
@Profile("test")
public class RateCacheServiceInMemory implements IRateCacheService {

    private final Map<String, RawRate> rawRateCache = new HashMap<>();
    private final Map<String, CalculatedRate> calculatedRateCache = new HashMap<>();
    private final Map<String, Double> usdmidCache = new HashMap<>();

    /**
     * Retrieves the cached USD mid value.
     *
     * @return Cached USD mid value, or null if not set.
     */
    @Override
    public Double getUSDMID() {
        String key = "usdmid";
        return usdmidCache.get(key);
    }

    /**
     * Saves the USD mid value in the cache.
     *
     * @param value The USD mid value to cache.
     * @return The saved value.
     */
    @Override
    public Double saveUSDMID(Double value) {
        String key = "usdmid";
        usdmidCache.put(key, value);
        return value;
    }

    /**
     * Retrieves a calculated rate by its type.
     *
     * @param rate The {@link CalculatedRate} containing the type to look up.
     * @return The cached {@link CalculatedRate} or null if not found.
     */
    @Override
    public CalculatedRate getCalcRate(CalculatedRate rate) {
        String key = String.format("calc_rates::rates:%s", rate.getType());
        return calculatedRateCache.get(key);
    }

    /**
     * Saves a calculated rate in the cache.
     *
     * @param rate The {@link CalculatedRate} to cache.
     * @return The saved rate.
     */
    @Override
    public CalculatedRate saveCalcRate(CalculatedRate rate) {
        String key = String.format("calc_rates::rates:%s", rate.getType());
        calculatedRateCache.put(key, rate);
        return rate;
    }

    /**
     * Retrieves a raw rate by provider and type.
     *
     * @param rate The {@link RawRate} containing provider and type.
     * @return The cached {@link RawRate} or null if not found.
     */
    @Override
    public RawRate getRawRate(RawRate rate) {
        String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());
        return rawRateCache.get(key);
    }

    /**
     * Saves a raw rate in the cache.
     *
     * @param rate The {@link RawRate} to cache.
     * @return The saved rate.
     */
    @Override
    public RawRate saveRawRate(RawRate rate) {
        String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());
        rawRateCache.put(key, rate);
        return rate;
    }

    /**
     * Retrieves all raw rates of a specific type.
     *
     * @param type The type of raw rates to retrieve.
     * @return List of matching {@link RawRate} instances, or an empty list if none found.
     * @throws CacheException Never thrown in this implementation, but declared by interface.
     */
    @Override
    public List<RawRate> getAllRawRatesForType(String type) throws CacheException {
        List<String> keys = rawRateCache.keySet()
                .stream()
                .filter(s -> s.startsWith("raw_rates::rates:") && s.endsWith(":" + type))
                .toList();

        if (keys.isEmpty()) {
            return List.of();
        }

        return keys.stream()
                .map(rawRateCache::get)
                .collect(Collectors.toList());
    }

    /**
     * Returns the name of this cache implementation.
     *
     * @return The string "InMemoryAPI".
     */
    @Override
    public String getName() {
        return "InMemoryAPI";
    }
}
