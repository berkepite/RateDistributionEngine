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

@Service
@Primary
@Profile({"debug", "dev", "prod"})
public class RateCacheServiceRedisAPI implements IRateCacheService {
    private final RedisTemplate<String, RawRate> rawRateRedisTemplate;
    private final RedisTemplate<String, CalculatedRate> calculatedRateRedisTemplate;
    private final RedisTemplate<String, Double> redisUSDMIDTemplate;

    public RateCacheServiceRedisAPI(RedisTemplate<String, RawRate> rawRateRedisTemplate, RedisTemplate<String, CalculatedRate> calculatedRateRedisTemplate, RedisTemplate<String, Double> redisUSDMIDTemplate) {
        this.rawRateRedisTemplate = rawRateRedisTemplate;
        this.calculatedRateRedisTemplate = calculatedRateRedisTemplate;
        this.redisUSDMIDTemplate = redisUSDMIDTemplate;
    }

    @Override
    public Double getUSDMID() throws CacheException {
        String key = "usdmid";
        try {
            return redisUSDMIDTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

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

    @Override
    public CalculatedRate getCalcRate(CalculatedRate rate) throws CacheException {
        try {
            String key = String.format("calc_rates::rates:%s", rate.getType());

            return calculatedRateRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

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

    @Override
    public RawRate getRawRate(RawRate rate) throws CacheException {
        try {
            String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());
            return rawRateRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }


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

    @Override
    public List<RawRate> getAllRawRatesForType(String type) throws CacheException {
        try {
            List<String> keys = rawRateRedisTemplate.keys("*")
                    .stream()
                    .filter(s -> s.startsWith("raw_rates::rates:") && s.endsWith(":" + type))
                    .toList();

            if (keys.isEmpty()) {
                return List.of(); // Return empty list if no matches
            }

            return keys.stream()
                    .map(key -> rawRateRedisTemplate.opsForValue().get(key)) // Fetch each entity
                    .collect(Collectors.toList()); // Collect results into a list

        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "RedisAPI";
    }
}
