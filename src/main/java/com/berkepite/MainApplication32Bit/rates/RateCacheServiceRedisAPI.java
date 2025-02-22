package com.berkepite.MainApplication32Bit.rates;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateCacheServiceRedisAPI implements IRateCacheService {
    private final RedisTemplate<String, RawRate> redisTemplate;

    public RateCacheServiceRedisAPI(RedisTemplate<String, RawRate> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RawRate getRate(RawRate rate) {
        String key = String.format("rates::rates:%s:%s", rate.getProvider(), rate.getType());

        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public RawRate getRawRate(RawRate rate) {
        String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());

        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public RawRate saveRate(RawRate rate) {
        String key = String.format("rates::rates:%s:%s", rate.getProvider(), rate.getType());

        redisTemplate.opsForValue().set(key, rate);
        return rate;
    }

    @Override
    public RawRate saveRawRate(RawRate rate) {
        String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());

        redisTemplate.opsForValue().set(key, rate);
        return rate;
    }
}
