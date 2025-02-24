package com.berkepite.MainApplication32Bit.rates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RateServiceTest {

    @Autowired
    @Qualifier("rateCacheServiceInMemory")
    private IRateCacheService rateCacheService;

    @Autowired
    private RateService rateService;

    @Autowired
    private RateFactory rateFactory;

    @Test
    public void shouldManageRawRate_USDTRY_whenRawRatesEmpty() {
        RawRate rawRate = rateFactory.createRawRate(
                RawRateEnum.USD_TRY.toString(), "arbitrary", 1.0, 2.0, Instant.now()
        );

        rateService.manageRawRate(rawRate);

        RawRate cachedRawRate = rateCacheService.getRawRate(rawRate);

        Assertions.assertThat(cachedRawRate.toString()).isEqualTo(rawRate.toString());
    }

    @Test
    public void shouldManageRawRate_USDTRY_whenRawRatesExist_USDMIDnull() {
        RawRate rawRateUSDTRY = rateFactory.createRawRate(
                RawRateEnum.USD_TRY.toString(), "arbitrary", 35.0, 35.0, null
        );
        RawRate rawRatetcpEURUSD = rateFactory.createRawRate(
                RawRateEnum.EUR_USD.toString(), "tcp", 1.0, 2.0, null
        );
        RawRate rawRatetcpGBPUSD = rateFactory.createRawRate(
                RawRateEnum.GBP_USD.toString(), "tcp", 1.0, 2.0, null
        );

        rateService.manageRawRate(rawRateUSDTRY);

        rateCacheService.saveRawRate(rawRatetcpEURUSD);
        rateCacheService.saveRawRate(rawRatetcpGBPUSD);

        CalculatedRate calcRateEURTRY = rateFactory.createCalcRate(CalculatedRateEnum.EUR_TRY.toString(), 0.0, 0.0, null);
        CalculatedRate calcRateGBPTRY = rateFactory.createCalcRate(CalculatedRateEnum.GBP_TRY.toString(), 0.0, 0.0, null);

        CalculatedRate cachedEURTRY = rateCacheService.getCalcRate(calcRateEURTRY);
        CalculatedRate cachedGBPTRY = rateCacheService.getCalcRate(calcRateGBPTRY);

        Assertions.assertThat(cachedEURTRY).isEqualTo(null);
        Assertions.assertThat(cachedGBPTRY).isEqualTo(null);
    }

    @Test
    public void shouldManageRawRate_USDTRY_whenRawRatesExist_USDMID35() {
        RawRate rawRateUSDTRY = rateFactory.createRawRate(
                RawRateEnum.USD_TRY.toString(), "CNN_TCP", 35.0, 35.0, null
        );
        RawRate rawRatetcpEURUSD = rateFactory.createRawRate(
                RawRateEnum.EUR_USD.toString(), "CNN_TCP", 1.0, 4.0, null
        );
        RawRate rawRatetcpGBPUSD = rateFactory.createRawRate(
                RawRateEnum.GBP_USD.toString(), "CNN_TCP", 3.0, 1.5, null
        );

        rateCacheService.saveRawRate(rawRatetcpEURUSD);
        rateCacheService.saveRawRate(rawRatetcpGBPUSD);
        rateCacheService.saveUSDMID(35.0);

        rateService.manageRawRate(rawRateUSDTRY);

        CalculatedRate calcRateEURTRY = rateFactory.createCalcRate(CalculatedRateEnum.EUR_TRY.toString(), 35.0, 140.0, null);
        CalculatedRate calcRateGBPTRY = rateFactory.createCalcRate(CalculatedRateEnum.GBP_TRY.toString(), 105.0, 52.5, null);

        CalculatedRate cachedEURTRY = rateCacheService.getCalcRate(calcRateEURTRY);
        CalculatedRate cachedGBPTRY = rateCacheService.getCalcRate(calcRateGBPTRY);

        Assertions.assertThat(cachedEURTRY.getBid()).isEqualTo(calcRateEURTRY.getBid());
        Assertions.assertThat(cachedEURTRY.getAsk()).isEqualTo(calcRateEURTRY.getAsk());
        Assertions.assertThat(cachedGBPTRY.getBid()).isEqualTo(calcRateGBPTRY.getBid());
        Assertions.assertThat(cachedGBPTRY.getAsk()).isEqualTo(calcRateGBPTRY.getAsk());
    }

}
