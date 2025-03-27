//package com.berkepite.RateDistributionEngine.rates;
//
//import com.berkepite.RateDistributionEngine.cache.IRateCacheService;
//import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
//import com.berkepite.RateDistributionEngine.common.rates.CalculatedRateEnum;
//import com.berkepite.RateDistributionEngine.common.rates.RawRate;
//import com.berkepite.RateDistributionEngine.common.rates.RawRateEnum;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.Instant;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class RateManagerTest {
//
//    @Autowired
//    @Qualifier("rateCacheServiceInMemory")
//    private IRateCacheService rateCacheService;
//
//    @Autowired
//    private RateManager rateManager;
//
//    @Autowired
//    private RateFactory rateFactory;
//
//    @Test
//    public void shouldManageRawRate_USDTRY_whenRawRatesEmpty() {
//        RawRate rawRate = rateFactory.createRawRate(
//                RawRateEnum.USD_TRY.toString(), "CNN_TCP", 1.0, 2.0, Instant.now()
//        );
//
//        rateManager.manageRawRate(rawRate);
//
//        RawRate cachedRawRate = rateCacheService.getRawRate(rawRate);
//
//        Assertions.assertThat(cachedRawRate.toString()).isEqualTo(rawRate.toString());
//    }
//
//    @Test
//    public void shouldManageRawRate_nonUSD_TRY_whenRawRatesEmpty() {
//        RawRate rawRate1 = rateFactory.createRawRate(
//                RawRateEnum.EUR_USD.toString(), "BLOOMBERG_REST", 1.0, 2.0, Instant.now()
//        );
//        RawRate rawRate2 = rateFactory.createRawRate(
//                RawRateEnum.GBP_USD.toString(), "BLOOMBERG_REST", 10.0, 20.0, Instant.now()
//        );
//
//        rateManager.manageRawRate(rawRate1);
//        rateManager.manageRawRate(rawRate2);
//
//        RawRate cachedRawRate1 = rateCacheService.getRawRate(rawRate1);
//        RawRate cachedRawRate2 = rateCacheService.getRawRate(rawRate2);
//
//        Assertions.assertThat(cachedRawRate1.toString()).isEqualTo(rawRate1.toString());
//        Assertions.assertThat(cachedRawRate2.toString()).isEqualTo(rawRate2.toString());
//    }
//
//    @Test
//    public void shouldManageRawRate_nonUSD_TRY_whenRawRatesEmpty_USDMID30() {
//        RawRate rawRateEURUSD = rateFactory.createRawRate(
//                RawRateEnum.EUR_USD.toString(), "CNN_TCP", 1.0, 2.0, null
//        );
//        RawRate rawRateGBPUSD = rateFactory.createRawRate(
//                RawRateEnum.GBP_USD.toString(), "CNN_TCP", 3.0, 4.0, null
//        );
//
//        rateCacheService.saveUSDMID(30.0);
//
//        rateManager.manageRawRate(rawRateEURUSD);
//        rateManager.manageRawRate(rawRateGBPUSD);
//
//        CalculatedRate calcRateEURTRY = rateFactory.createCalcRate(CalculatedRateEnum.EUR_TRY.toString(), 30.0, 60.0, null);
//        CalculatedRate calcRateGBPTRY = rateFactory.createCalcRate(CalculatedRateEnum.GBP_TRY.toString(), 90.0, 120.0, null);
//
//        CalculatedRate cachedEURTRY = rateCacheService.getCalcRate(calcRateEURTRY);
//        CalculatedRate cachedGBPTRY = rateCacheService.getCalcRate(calcRateGBPTRY);
//
//        Assertions.assertThat(cachedEURTRY.getBid()).isEqualTo(calcRateEURTRY.getBid());
//        Assertions.assertThat(cachedEURTRY.getAsk()).isEqualTo(calcRateEURTRY.getAsk());
//        Assertions.assertThat(cachedGBPTRY.getBid()).isEqualTo(calcRateGBPTRY.getBid());
//        Assertions.assertThat(cachedGBPTRY.getAsk()).isEqualTo(calcRateGBPTRY.getAsk());
//    }
//
//    @Test
//    public void shouldManageRawRate_nonUSD_TRY_whenRawRatesExist_USDMID10() {
//        // Should not have %1 difference in order to calculate
//
//        RawRate rawRateInCacheEURUSD = rateFactory.createRawRate(
//                RawRateEnum.EUR_USD.toString(), "BLOOMBERG_REST", 1.0, 2.0, null
//        );
//        RawRate rawRateEURUSD = rateFactory.createRawRate(
//                RawRateEnum.EUR_USD.toString(), "CNN_TCP", 1.005, 2.02, null
//        );
//        RawRate rawRateInCacheGBPUSD = rateFactory.createRawRate(
//                RawRateEnum.GBP_USD.toString(), "BLOOMBERG_REST", 3.0, 4.0, null
//        );
//        RawRate rawRateGBPUSD = rateFactory.createRawRate(
//                RawRateEnum.GBP_USD.toString(), "CNN_TCP", 3.01, 4.04, null
//        );
//
//        rateCacheService.saveRawRate(rawRateInCacheEURUSD);
//        rateCacheService.saveRawRate(rawRateInCacheGBPUSD);
//        rateCacheService.saveUSDMID(10.0);
//
//        rateManager.manageRawRate(rawRateEURUSD);
//        rateManager.manageRawRate(rawRateGBPUSD);
//
//        CalculatedRate calcRateEURTRY = rateFactory.createCalcRate(CalculatedRateEnum.EUR_TRY.toString(), 10.025, 20.1, null);
//        CalculatedRate calcRateGBPTRY = rateFactory.createCalcRate(CalculatedRateEnum.GBP_TRY.toString(), 30.05, 40.2, null);
//
//        CalculatedRate cachedEURTRY = rateCacheService.getCalcRate(calcRateEURTRY);
//        CalculatedRate cachedGBPTRY = rateCacheService.getCalcRate(calcRateGBPTRY);
//
//        Assertions.assertThat(cachedEURTRY.getBid()).isEqualTo(calcRateEURTRY.getBid());
//        Assertions.assertThat(cachedEURTRY.getAsk()).isEqualTo(calcRateEURTRY.getAsk());
//        Assertions.assertThat(cachedGBPTRY.getBid()).isEqualTo(calcRateGBPTRY.getBid());
//        Assertions.assertThat(cachedGBPTRY.getAsk()).isEqualTo(calcRateGBPTRY.getAsk());
//    }
//}
