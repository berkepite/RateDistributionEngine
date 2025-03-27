package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.rates.RateConverter;
import com.berkepite.RateDistributionEngine.rates.RateFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PythonCalculatorTest {
    private PythonCalculator pythonCalculator;

    @Mock
    private RateConverter rateConverter;
    @Mock
    private CalculatorLoader calculatorLoader;

    @BeforeEach
    public void setUp() {
        RateFactory rateFactory = new RateFactory();
        pythonCalculator = new PythonCalculator(rateFactory, rateConverter, calculatorLoader);
    }

    @Test
    public void shouldHaveAtLeastOnePercentDiff() throws Exception {
        RawRate rate1 = createRateWithBidAndAsk(1.005, 2.1);
        RawRate rate2 = createRateWithBidAndAsk(1.01, 2);

        Boolean result = pythonCalculator.hasAtLeastOnePercentDiff(rate1, rate2);

        Assertions.assertEquals(true, result);
    }

    @Test
    public void shouldCalculateMean_Rate_OfRawRates_return5BidAnd4Ask() throws Exception {
        RawRate incomingRate = createRateWithBidAndAsk(2, 2);
        List<RawRate> otherPlatformRates = new ArrayList<>();
        otherPlatformRates.add(createRateWithBidAndAsk(8, 6));
        otherPlatformRates.add(createRateWithBidAndAsk(2, 2));

        List<Double[]> values = getBidsAndAsks(otherPlatformRates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        RawRate rate = pythonCalculator.calculateMeanRate(incomingRate, bids, asks);

        Assertions.assertEquals(5, rate.getBid());
        Assertions.assertEquals(4, rate.getAsk());
    }

    @Test
    public void shouldCalculateMean_Rate_OfRawRates_return0_15BidAnd3_5Ask() throws Exception {
        RawRate incomingRate = createRateWithBidAndAsk(0.5, 0.5);
        List<RawRate> otherPlatformRates = new ArrayList<>();
        otherPlatformRates.add(createRateWithBidAndAsk(0.2, 0.4));
        otherPlatformRates.add(createRateWithBidAndAsk(0.1, 0.3));

        List<Double[]> values = getBidsAndAsks(otherPlatformRates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        RawRate rate = pythonCalculator.calculateMeanRate(incomingRate, bids, asks);

        Assertions.assertEquals(0.15, rate.getBid());
        Assertions.assertEquals(0.35, rate.getAsk());
    }

    @Test
    public void shouldCalculateUSDMID_return35_499375() throws Exception {
        List<RawRate> rates = new ArrayList<>();
        rates.add(createRateWithBidAndAsk(35.6655, 36.7765));
        rates.add(createRateWithBidAndAsk(34.1234, 35.4321));

        List<Double[]> values = getBidsAndAsks(rates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        Double usdmid = pythonCalculator.calculateUSDMID(bids, asks);

        Assertions.assertEquals(35.499375, usdmid);
    }

    @Test
    public void shouldCalculateForUSD_TRY_return1_0335BidAnd1_039Ask() throws Exception {
        List<RawRate> rates = new ArrayList<>();
        rates.add(createRateWithBidAndAsk(1.022, 1.037));
        rates.add(createRateWithBidAndAsk(1.045, 1.041));

        List<Double[]> values = getBidsAndAsks(rates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        CalculatedRate calcRate = pythonCalculator.calculateForUSD_TRY(bids, asks);

        Assertions.assertEquals(1.0335, calcRate.getBid());
        Assertions.assertEquals(1.039, calcRate.getAsk());
    }

    @Test
    public void shouldCalculateForEUR_USD_return36_017475BidAnd36_20915Ask() throws Exception {
        List<RawRate> rates = new ArrayList<>();
        rates.add(createRateWithBidAndAsk(1.022, 1.037));
        rates.add(createRateWithBidAndAsk(1.045, 1.041));

        List<Double[]> values = getBidsAndAsks(rates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        Double usdmid = 34.85;

        CalculatedRate calcRate = pythonCalculator.calculateForRawRateType("EUR_USD", usdmid, bids, asks);

        Assertions.assertEquals(36.017475, calcRate.getBid());
        Assertions.assertEquals(36.20915, calcRate.getAsk());
    }

    private RawRate createRateWithBidAndAsk(double bid, double ask) throws Exception {
        RawRate rate = new RawRate();
        rate.setBid(bid);
        rate.setAsk(ask);
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
