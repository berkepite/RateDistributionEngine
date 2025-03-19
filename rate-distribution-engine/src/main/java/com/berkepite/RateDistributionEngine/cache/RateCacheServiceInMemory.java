package com.berkepite.RateDistributionEngine.cache;

import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("test")
public class RateCacheServiceInMemory implements IRateCacheService {
    private final Map<String, RawRate> rawRateCache = new HashMap<>();
    private final Map<String, CalculatedRate> calculatedRateCache = new HashMap<>();
    private final Map<String, Double> usdmidCache = new HashMap<>();

    @Override
    public Double getUSDMID() {
        String key = "usdmid";

        return usdmidCache.get(key);
    }

    @Override
    public Double saveUSDMID(Double value) {
        String key = "usdmid";

        usdmidCache.put(key, value);
        return value;
    }

    @Override
    public CalculatedRate getCalcRate(CalculatedRate rate) {
        String key = String.format("calc_rates::rates:%s", rate.getType());

        return calculatedRateCache.get(key);
    }

    @Override
    public CalculatedRate saveCalcRate(CalculatedRate rate) {
        String key = String.format("calc_rates::rates:%s", rate.getType());

        calculatedRateCache.put(key, rate);
        return rate;
    }

    @Override
    public RawRate getRawRate(RawRate rate) {
        String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());

        return rawRateCache.get(key);
    }


    @Override
    public RawRate saveRawRate(RawRate rate) {
        String key = String.format("raw_rates::rates:%s:%s", rate.getProvider(), rate.getType());

        rawRateCache.put(key, rate);
        return rate;
    }

    @Override
    public List<RawRate> getAllRawRatesForType(String type) {

        List<String> keys = rawRateCache.keySet()
                .stream()
                .filter(s -> s.startsWith("raw_rates::rates:") && s.endsWith(":" + type))
                .toList();


        if (keys.isEmpty()) {
            return List.of(); // Return empty list if no matches
        }

        return keys.stream()
                .map(rawRateCache::get) // Fetch each entity
                .collect(Collectors.toList()); // Collect results into a list
    }
}
