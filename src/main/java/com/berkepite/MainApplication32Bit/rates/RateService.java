package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberEnum;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RateService {
    private final IRateRepository rateRepository;

    public RateService(IRateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    @Cacheable(value = "rates", key = "'rates:' + #provider.toString() + ':' + #rate.toString() + ':' + #instant.toString()")
    public RateEntity getRate(SubscriberEnum provider, RateEnum rate, Instant instant) {
        return rateRepository.findByProviderAndRateAndTimestamp(provider, rate, instant)
                .orElse(null); // Return the fetched rate or null if not found
    }

    @Cacheable(value = "rates", key = "'rates:ALL'")
    public List<RateEntity> getAllRates() {
        return rateRepository.findAll();
    }

    @CachePut(value = "rates", key = "'rates:' + #rate.provider.toString() + ':' + #rate.rate.toString() + ':' + #rate.timestamp.toString()")
    public RateEntity saveRate(RateEntity rate) {
        return rateRepository.save(rate);
    }
}
