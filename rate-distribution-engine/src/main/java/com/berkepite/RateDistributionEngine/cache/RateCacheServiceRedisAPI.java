package com.berkepite.RateDistributionEngine.cache;

import com.berkepite.RateDistributionEngine.common.cache.IRateCacheService;
import com.berkepite.RateDistributionEngine.common.exception.cache.CacheException;
import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis-backed implementation of the {@link IRateCacheService} interface.
 * <p>
 * This service uses Spring Data RedisTemplate to perform caching operations for raw rates,
 * calculated rates, and USD mid rates. It provides methods to save and retrieve rates
 * from a Redis cache, handling exceptions by wrapping them in {@link CacheException}.
 * </p>
 * <p>
 * This implementation is marked as {@code @Primary} and active under the {@code debug}, {@code dev},
 * and {@code prod} Spring profiles.
 * </p>
 */
@Service
@Primary
@Profile({"debug", "dev", "prod"})
public class RateCacheServiceRedisAPI implements IRateCacheService {

    private final RedisTemplate<String, RawRate> rawRateRedisTemplate;
    private final RedisTemplate<String, CalculatedRate> calculatedRateRedisTemplate;
    private final RedisTemplate<String, Double> redisUSDMIDTemplate;

    /**
     * Constructs a RateCacheServiceRedisAPI instance with the provided Redis templates.
     *
     * @param rawRateRedisTemplate        Redis template for raw rates.
     * @param calculatedRateRedisTemplate Redis template for calculated rates.
     * @param redisUSDMIDTemplate         Redis template for USD mid values.
     */
    public RateCacheServiceRedisAPI(RedisTemplate<String, RawRate> rawRateRedisTemplate,
                                    RedisTemplate<String, CalculatedRate> calculatedRateRedisTemplate,
                                    RedisTemplate<String, Double> redisUSDMIDTemplate) {
        this.rawRateRedisTemplate = rawRateRedisTemplate;
        this.calculatedRateRedisTemplate = calculatedRateRedisTemplate;
        this.redisUSDMIDTemplate = redisUSDMIDTemplate;
    }

    /**
     * Retrieves the cached USD mid value from Redis.
     *
     * @return Cached USD mid price, or null if not present.
     * @throws CacheException If Redis access fails.
     */
    @Override
    public Double getUSDMID() throws CacheException {
        String key = "usdmid";
        try {
            return redisUSDMIDTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    /**
     * Saves the given USD mid value into the Redis cache.
     *
     * @param value The USD mid price to cache.
     * @return The saved USD mid value.
     * @throws CacheException If Redis access fails.
     */
    @Override
    public Double saveUSDMID(Double value) throws CacheException {
        String key = "usdmid";
        try {
            redisUSDMIDTemplate.opsForValue().set(key, value);
            return value;
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves a cached calculated rate from Redis by its type.
     *
     * @param rate The {@link CalculatedRate} object containing the rate type.
     * @return Cached {@link CalculatedRate} instance, or null if not found.
     * @throws CacheException If Redis access fails.
     */
    @Override
    public CalculatedRate getCalcRate(CalculatedRate rate) throws CacheException {
        try {
            String key = String.format("calc_rates::rates:%s", rate.getType());
            return calculatedRateRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    /**
     * Saves a calculated rate into Redis cache.
     *
     * @param rate The {@link CalculatedRate} to save.
     * @return The saved {@link CalculatedRate}.
     * @throws CacheException If Redis access fails.
     */
    @Override
    public CalculatedRate saveCalcRate(CalculatedRate rate) throws CacheException {
        try {
            String key = String.format("calc_rates::rates:%s", rate.getType());
            calculatedRateRedisTemplate.opsForValue().set(key, rate);
            return rate;
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves a cached raw rate from Redis by provider and type.
     *
     * @param rate The {@link RawRate} object containing provider and type.
     * @return Cached {@link RawRate}, or null if not found.
     * @throws CacheException If Redis access fails.
     */
    @Override
    public RawRate getRawRate(RawRate rate) throws CacheException {
        try {
            String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());
            return rawRateRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    /**
     * Saves a raw rate into Redis cache.
     *
     * @param rate The {@link RawRate} to save.
     * @return The saved {@link RawRate}.
     * @throws CacheException If Redis access fails.
     */
    @Override
    public RawRate saveRawRate(RawRate rate) throws CacheException {
        try {
            String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());
            rawRateRedisTemplate.opsForValue().set(key, rate);
            return rate;
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves all cached raw rates of a specified type.
     *
     * @param type The type of raw rates to retrieve.
     * @return List of all matching {@link RawRate} entries, or an empty list if none found.
     * @throws CacheException If Redis access fails.
     */
    @Override
    public List<RawRate> getAllRawRatesForType(String type) throws CacheException {
        try {
            List<String> keys = rawRateRedisTemplate.keys("*")
                    .stream()
                    .filter(s -> s.startsWith("raw_rates::rates:") && s.endsWith(":" + type))
                    .toList();

            if (keys.isEmpty()) {
                return List.of();
            }

            return keys.stream()
                    .map(key -> rawRateRedisTemplate.opsForValue().get(key))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    /**
     * Returns the name of this cache service implementation.
     *
     * @return The string "RedisAPI".
     */
    @Override
    public String getName() {
        return "RedisAPI";
    }
}
