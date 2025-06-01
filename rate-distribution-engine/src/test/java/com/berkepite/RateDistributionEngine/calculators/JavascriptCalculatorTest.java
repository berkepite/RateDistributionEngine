package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorLoadingException;
import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.MeanRate;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.rates.RateConverter;
import com.berkepite.RateDistributionEngine.rates.RateFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class JavascriptCalculatorTest {

    @Test
    public void shouldHaveAtLeastOnePercentDiff() throws Exception {
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/js-test-valid.mjs");

        RawRate rate1 = createRateWithBidAndAsk(1.005, 2.1);
        MeanRate rate2 = createMeanRate(1.01, 2);

        Boolean result = javascriptCalculator.hasAtLeastOnePercentDiff(rate1, rate2);

        Assertions.assertEquals(true, result);
    }

    @Test
    public void shouldCalculateMean_Rate_OfRawRates_return5BidAnd4Ask() throws Exception {
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/js-test-valid.mjs");

        RawRate incomingRate = createRateWithBidAndAsk(2, 2);
        List<RawRate> otherPlatformRates = new ArrayList<>();
        otherPlatformRates.add(createRateWithBidAndAsk(8, 6));
        otherPlatformRates.add(createRateWithBidAndAsk(2, 2));

        List<Double[]> values = getBidsAndAsks(otherPlatformRates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        MeanRate rate = javascriptCalculator.calculateMeanRate(bids, asks);

        Assertions.assertEquals(5, rate.getMeanBid());
        Assertions.assertEquals(4, rate.getMeanAsk());
    }

    @Test
    public void shouldCalculateMean_Rate_OfRawRates_return0_15BidAnd3_5Ask() throws Exception {
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/js-test-valid.mjs");

        RawRate incomingRate = createRateWithBidAndAsk(0.5, 0.5);
        List<RawRate> otherPlatformRates = new ArrayList<>();
        otherPlatformRates.add(createRateWithBidAndAsk(0.2, 0.4));
        otherPlatformRates.add(createRateWithBidAndAsk(0.1, 0.3));

        List<Double[]> values = getBidsAndAsks(otherPlatformRates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        MeanRate rate = javascriptCalculator.calculateMeanRate(bids, asks);

        Assertions.assertEquals(0.15, rate.getMeanBid());
        Assertions.assertEquals(0.35, rate.getMeanAsk());
    }

    @Test
    public void shouldCalculateUSDMID_return35_499375() throws Exception {
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/js-test-valid.mjs");

        List<RawRate> rates = new ArrayList<>();
        rates.add(createRateWithBidAndAsk(35.6655, 36.7765));
        rates.add(createRateWithBidAndAsk(34.1234, 35.4321));

        List<Double[]> values = getBidsAndAsks(rates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        Double usdmid = javascriptCalculator.calculateUSDMID(bids, asks);

        Assertions.assertEquals(35.499375, usdmid);
    }

    @Test
    public void shouldCalculateForUSD_TRY_return1_0335BidAnd1_039Ask() throws Exception {
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/js-test-valid.mjs");

        List<RawRate> rates = new ArrayList<>();
        rates.add(createRateWithBidAndAsk(1.022, 1.037));
        rates.add(createRateWithBidAndAsk(1.045, 1.041));

        List<Double[]> values = getBidsAndAsks(rates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        CalculatedRate calcRate = javascriptCalculator.calculateForUSD_TRY(bids, asks);

        Assertions.assertEquals(1.0335, calcRate.getBid());
        Assertions.assertEquals(1.039, calcRate.getAsk());
    }

    @Test
    public void shouldCalculateForEUR_USD_return36_017475BidAnd36_20915Ask() throws Exception {
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/js-test-valid.mjs");

        List<RawRate> rates = new ArrayList<>();
        rates.add(createRateWithBidAndAsk(1.022, 1.037));
        rates.add(createRateWithBidAndAsk(1.045, 1.041));

        List<Double[]> values = getBidsAndAsks(rates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        Double usdmid = 34.85;

        CalculatedRate calcRate = javascriptCalculator.calculateForRawRateType("EUR_USD", usdmid, bids, asks);

        Assertions.assertEquals(36.017475, calcRate.getBid());
        Assertions.assertEquals(36.20915, calcRate.getAsk());
    }

    @Test
    void init_shouldThrowCalculatorLoadingException_whenFileNotFound() {
        JavascriptCalculator calculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());

        assertThrows(CalculatorLoadingException.class, () -> {
            calculator.init("nonexistent/path/to/script.mjs");
        });
    }

    @Test
    void calculateMeanRate_shouldThrowCalculatorException_whenFunctionMissing() throws CalculatorException {
        // Use a test script that doesn't define `calculateMeanRate`
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/faulty_scripts/no_calculateMean.mjs");

        Exception exception = assertThrows(CalculatorException.class, () -> {
            javascriptCalculator.calculateMeanRate(new Double[]{1.0}, new Double[]{2.0});
        });

        assertTrue(exception.getMessage().contains("Failed to calculate mean rate"));
    }

    @Test
    void calculateUSDMID_shouldThrowCalculatorException_whenJsThrowsError() throws CalculatorException {
        // Use a script where calculateUSDMID throws an exception
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/faulty_scripts/throws_in_usdmid.mjs");

        Exception exception = assertThrows(CalculatorException.class, () -> {
            javascriptCalculator.calculateUSDMID(new Double[]{1.0}, new Double[]{2.0});
        });

        assertTrue(exception.getMessage().contains("Failed to calculate for usdmid"));
    }

    @Test
    void calculateMeanRate_shouldThrowCalculatorException_whenReturnIsNotArray() throws CalculatorException {
        JavascriptCalculator javascriptCalculator = new JavascriptCalculator(new RateFactory(), new RateConverter(), new CalculatorLoader());
        javascriptCalculator.init("src/test/resources/rate_calculators/faulty_scripts/returns_object_instead_of_array.mjs");

        assertThrows(CalculatorException.class, () -> {
            javascriptCalculator.calculateMeanRate(new Double[]{1.0}, new Double[]{2.0});
        });
    }

    private RawRate createRateWithBidAndAsk(double bid, double ask) throws Exception {
        RawRate rate = new RawRate();
        rate.setBid(bid);
        rate.setAsk(ask);
        return rate;
    }

    private MeanRate createMeanRate(double bid, double ask) throws Exception {
        MeanRate rate = new MeanRate();
        rate.setMeanBid(bid);
        rate.setMeanAsk(ask);
        return rate;
    }

    private List<Double[]> getBidsAndAsks(List<RawRate> rates) {
        List<Double> bids = new ArrayList<>();
        rates.forEach(platformRate -> bids.add(platformRate.getBid()));

        List<Double> asks = new ArrayList<>();
        rates.forEach(platformRate -> asks.add(platformRate.getAsk()));

        return Arrays.asList(bids.toArray(new Double[0]), asks.toArray(new Double[0]));
    }
}
