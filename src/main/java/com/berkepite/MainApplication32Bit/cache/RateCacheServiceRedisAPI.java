package com.berkepite.MainApplication32Bit.cache;

import com.berkepite.MainApplication32Bit.rates.CalculatedRate;
import com.berkepite.MainApplication32Bit.rates.RawRate;
import com.berkepite.MainApplication32Bit.subscribers.SubscriberEnum;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
@Profile({"debug", "prod"})
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
    public Double getUSDMID() {
        String key = "usdmid";

        return redisUSDMIDTemplate.opsForValue().get(key);
    }

    @Override
    public Double saveUSDMID(Double value) {
        String key = "usdmid";

        redisUSDMIDTemplate.opsForValue().set(key, value);
        return value;
    }

    @Override
    public CalculatedRate getCalcRate(CalculatedRate rate) {
        String key = String.format("calc_rates::rates:%s", rate.getType());

        return calculatedRateRedisTemplate.opsForValue().get(key);
    }

    @Override
    public CalculatedRate saveCalcRate(CalculatedRate rate) {
        String key = String.format("calc_rates::rates:%s", rate.getType());

        calculatedRateRedisTemplate.opsForValue().set(key, rate);
        return rate;
    }

    @Override
    public RawRate getRawRate(RawRate rate) {
        String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());

        return rawRateRedisTemplate.opsForValue().get(key);
    }


    @Override
    public RawRate saveRawRate(RawRate rate) {
        String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());

        rawRateRedisTemplate.opsForValue().set(key, rate);
        return rate;
    }

    @Override
    public List<RawRate> getAllRawRatesForType(String type) {

        List<String> providers = Arrays.stream(SubscriberEnum.values())
                .map(Enum::toString)
                .toList();

        Set<String> keys = new HashSet<>();

        for (String provider : providers) {
            String pattern = "raw_rates::rates:" + provider + ":" + type;
            keys.addAll(rawRateRedisTemplate.keys(pattern));
        }

        if (keys.isEmpty()) {
            return List.of(); // Return empty list if no matches
        }

        return keys.stream()
                .map(key -> rawRateRedisTemplate.opsForValue().get(key)) // Fetch each entity
                .collect(Collectors.toList()); // Collect results into a list
    }
}
