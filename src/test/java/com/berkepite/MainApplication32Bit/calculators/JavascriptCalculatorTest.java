package com.berkepite.MainApplication32Bit.calculators;

import com.berkepite.MainApplication32Bit.rates.RawRate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavascriptCalculatorTest {

    static JavascriptCalculator javascriptCalculator = new JavascriptCalculator("rate_calculators/javascript-test.mjs");

    @BeforeAll
    public static void setUp() throws IOException {
        javascriptCalculator.init();
    }

    @Test
    public void shouldHaveAtLeastOnePercentDiff() throws Exception {
        RawRate rate1 = createRateWithBidAndAsk(1.005, 2.1);
        RawRate rate2 = createRateWithBidAndAsk(1.01, 2);

        Boolean result = javascriptCalculator.hasAtLeastOnePercentDiff(rate1, rate2);

        Assertions.assertEquals(true, result);
    }

    @Test
    public void shouldCalculateMean_return5bidAnd4ask() throws Exception {
        RawRate incomingRate = createRateWithBidAndAsk(2, 2);
        List<RawRate> otherPlatformRates = new ArrayList<>();
        otherPlatformRates.add(createRateWithBidAndAsk(8, 6));

        RawRate rate = javascriptCalculator.calculateMean(incomingRate, otherPlatformRates);

        Assertions.assertEquals(5, rate.getBid());
        Assertions.assertEquals(4, rate.getAsk());
    }

    @Test
    public void shouldCalculateMean_return0_15bidAnd0_15ask() throws Exception {
        RawRate incomingRate = createRateWithBidAndAsk(0.1, 0.1);
        List<RawRate> otherPlatformRates = new ArrayList<>();
        otherPlatformRates.add(createRateWithBidAndAsk(0.2, 0.2));

        RawRate rate = javascriptCalculator.calculateMean(incomingRate, otherPlatformRates);

        Assertions.assertEquals(0.15, rate.getBid());
        Assertions.assertEquals(0.15, rate.getAsk());
    }

    @Test
    public void shouldCalculateUSDMID_return35_499375() throws Exception {
        List<RawRate> rates = new ArrayList<>();
        rates.add(createRateWithBidAndAsk(35.6655, 36.7765));
        rates.add(createRateWithBidAndAsk(34.1234, 35.4321));


        Double usdmid = javascriptCalculator.calculateUSDMID(rates);

        Assertions.assertEquals(35.499375, usdmid);
    }

    private RawRate createRateWithBidAndAsk(double bid, double ask) throws Exception {
        RawRate rate = new RawRate();
        rate.setBid(bid);
        rate.setAsk(ask);
        return rate;
    }
}
