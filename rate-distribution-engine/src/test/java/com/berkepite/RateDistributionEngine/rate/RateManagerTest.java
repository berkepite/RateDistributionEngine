package com.berkepite.RateDistributionEngine.rate;

import com.berkepite.RateDistributionEngine.cache.RateCacheServiceInMemory;
import com.berkepite.RateDistributionEngine.common.cache.IRateCacheService;
import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

@SpringBootTest
@Import(TestKafkaConfig.class)
@ActiveProfiles("test")
public class RateManagerTest {

    @Autowired
    @Qualifier("rateCacheServiceInMemory")
    private IRateCacheService rateCacheService;

    @Autowired
    private RateManager rateManager;

    @Autowired
    private RateFactory rateFactory;

    @Test
    public void shouldManageRawRate_USDTRY_whenRawRatesEmpty() throws Exception {
        RawRate rawRate = rateFactory.createRawRate(
                "USD_TRY", "TCP", 1.0, 2.0, Instant.now()
        );

        rateManager.manageIncomingRawRate(rawRate);

        RawRate cachedRawRate = rateCacheService.getRawRate(rawRate);

        Assertions.assertThat(cachedRawRate.toString()).isEqualTo(rawRate.toString());
    }

    @Test
    public void shouldManageRawRate_nonUSD_TRY_whenRawRatesEmpty() throws Exception {
        RawRate rawRate1 = rateFactory.createRawRate(
                "EUR_USD", "BLOOMBERG_REST", 1.0, 2.0, Instant.now()
        );
        RawRate rawRate2 = rateFactory.createRawRate(
                "GBP_USD", "BLOOMBERG_REST", 10.0, 20.0, Instant.now()
        );

        rateManager.manageIncomingRawRate(rawRate1);
        rateManager.manageIncomingRawRate(rawRate2);

        RawRate cachedRawRate1 = rateCacheService.getRawRate(rawRate1);
        RawRate cachedRawRate2 = rateCacheService.getRawRate(rawRate2);

        Assertions.assertThat(cachedRawRate1.toString()).isEqualTo(rawRate1.toString());
        Assertions.assertThat(cachedRawRate2.toString()).isEqualTo(rawRate2.toString());
    }

    @Test
    public void shouldManageRawRate_nonUSD_TRY_whenRawRatesEmpty_USDMID30() throws Exception {
        RawRate rawRateEURUSD = rateFactory.createRawRate(
                "EUR_USD", "CNN_TCP", 1.0, 2.0, null
        );
        RawRate rawRateGBPUSD = rateFactory.createRawRate(
                "GBP_USD", "CNN_TCP", 3.0, 4.0, null
        );

        rateCacheService.saveUSDMID(30.0);

        rateManager.manageIncomingRawRate(rawRateEURUSD);
        rateManager.manageIncomingRawRate(rawRateGBPUSD);

        CalculatedRate calcRateEURTRY = rateFactory.createCalcRate("EUR_TRY", 30.0, 60.0, null);
        CalculatedRate calcRateGBPTRY = rateFactory.createCalcRate("GBP_TRY", 90.0, 120.0, null);

        CalculatedRate cachedEURTRY = rateCacheService.getCalcRate(calcRateEURTRY);
        CalculatedRate cachedGBPTRY = rateCacheService.getCalcRate(calcRateGBPTRY);

        Assertions.assertThat(cachedEURTRY.getBid()).isEqualTo(calcRateEURTRY.getBid());
        Assertions.assertThat(cachedEURTRY.getAsk()).isEqualTo(calcRateEURTRY.getAsk());
        Assertions.assertThat(cachedGBPTRY.getBid()).isEqualTo(calcRateGBPTRY.getBid());
        Assertions.assertThat(cachedGBPTRY.getAsk()).isEqualTo(calcRateGBPTRY.getAsk());
    }

    @Test
    public void shouldManageRawRate_nonUSD_TRY_whenRawRatesExist_USDMID10() throws Exception {
        // Should not have %1 difference in order to calculate

        RawRate rawRateInCacheEURUSD = rateFactory.createRawRate(
                "EUR_USD", "BLOOMBERG_REST", 1.0, 2.0, null
        );
        RawRate rawRateEURUSD = rateFactory.createRawRate(
                "EUR_USD", "CNN_TCP", 1.005, 2.02, null
        );
        RawRate rawRateInCacheGBPUSD = rateFactory.createRawRate(
                "GBP_USD", "BLOOMBERG_REST", 3.0, 4.0, null
        );
        RawRate rawRateGBPUSD = rateFactory.createRawRate(
                "GBP_USD", "CNN_TCP", 3.01, 4.04, null
        );

        rateCacheService.saveRawRate(rawRateInCacheEURUSD);
        rateCacheService.saveRawRate(rawRateInCacheGBPUSD);
        rateCacheService.saveUSDMID(10.0);

        rateManager.manageIncomingRawRate(rawRateEURUSD);
        rateManager.manageIncomingRawRate(rawRateGBPUSD);

        CalculatedRate calcRateEURTRY = rateFactory.createCalcRate("EUR_TRY", 10.025, 20.1, null);
        CalculatedRate calcRateGBPTRY = rateFactory.createCalcRate("GBP_TRY", 30.05, 40.2, null);

        CalculatedRate cachedEURTRY = rateCacheService.getCalcRate(calcRateEURTRY);
        CalculatedRate cachedGBPTRY = rateCacheService.getCalcRate(calcRateGBPTRY);

        Assertions.assertThat(cachedEURTRY.getBid()).isEqualTo(calcRateEURTRY.getBid());
        Assertions.assertThat(cachedEURTRY.getAsk()).isEqualTo(calcRateEURTRY.getAsk());
        Assertions.assertThat(cachedGBPTRY.getBid()).isEqualTo(calcRateGBPTRY.getBid());
        Assertions.assertThat(cachedGBPTRY.getAsk()).isEqualTo(calcRateGBPTRY.getAsk());
    }
}
