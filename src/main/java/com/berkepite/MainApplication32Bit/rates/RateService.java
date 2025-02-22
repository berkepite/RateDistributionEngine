package com.berkepite.MainApplication32Bit.rates;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class RateService {
    private final IRateCacheService rateCacheService;

    public RateService(@Qualifier("rateCacheServiceRedisAPI") IRateCacheService rateCacheService) {
        this.rateCacheService = rateCacheService;
    }

    public void manageRawRate(RawRate rawRate) {
        rateCacheService.saveRawRate(rawRate);
        RawRate latestRate = rateCacheService.getRate(rawRate);
    }


}
