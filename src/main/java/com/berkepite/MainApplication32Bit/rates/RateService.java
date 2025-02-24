package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.calculators.CalculatorFactory;
import com.berkepite.MainApplication32Bit.calculators.IRateCalculator;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RateService {
    private final Logger LOGGER = LogManager.getLogger(RateService.class);

    private IRateCalculator rateCalculator;
    private final CalculatorFactory calculatorFactory;
    private final IRateCacheService rateCacheService;

    @Value("${app.rate-calculation-strategy}")
    private String rateCalculationStrategy;
    @Value("${app.rate-calculation-source-path}")
    private String rateCalculationSourcePath;

    public RateService(IRateCacheService rateCacheService, CalculatorFactory calculatorFactory) {
        this.rateCacheService = rateCacheService;
        this.calculatorFactory = calculatorFactory;
    }

    @PostConstruct
    public void init() {
        rateCalculator = calculatorFactory.getCalculator(rateCalculationStrategy, rateCalculationSourcePath);
    }

    public void manageRawRate(RawRate incomingRate) {
        // write to raw database
        List<RawRate> allRawRatesForType = rateCacheService.getAllRawRatesForType(incomingRate.getType());

        List<Double[]> values = getBidsAndAsks(allRawRatesForType);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        RawRate meanRawRate = rateCalculator.calculateMeansOfRawRates(incomingRate, bids, asks);

        if (!rateCalculator.hasAtLeastOnePercentDiff(meanRawRate.getBid(), meanRawRate.getAsk(), incomingRate.getBid(), incomingRate.getAsk())) {
            LOGGER.info("has one p diff: {}", incomingRate.getType());
            rateCacheService.saveRawRate(incomingRate);

            if (incomingRate.getType().equals(RawRateEnum.USD_TRY.toString())) {
                calculateAndSaveUSDMID();

                Arrays.stream(RawRateEnum.values()).forEach(
                        val ->
                                calculateAndSaveForType(val.toString())
                );

            } else {
                calculateAndSaveForType(incomingRate.getType());
            }
        }
    }

    private void calculateAndSaveForType(String type) {
        Double usdmid = rateCacheService.getUSDMID();
        List<RawRate> allRawRates = rateCacheService.getAllRawRatesForType(type);

        List<Double[]> values = getBidsAndAsks(allRawRates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        CalculatedRate calcRate;

        switch (type) {
            case "USD_TRY" -> {
                if (asks.length != 0 && bids.length != 0) {
                    calcRate = rateCalculator.calculateForUSD_TRY(bids, asks);
                    rateCacheService.saveCalcRate(calcRate);
                    //save to database
                }
            }
            case "EUR_USD" -> {
                if (usdmid != null && asks.length != 0 && bids.length != 0) {
                    calcRate = rateCalculator.calculateForType("EUR_TRY", usdmid, bids, asks);
                    rateCacheService.saveCalcRate(calcRate);
                    //save to database
                }
            }
            case "GBP_USD" -> {
                if (usdmid != null && asks.length != 0 && bids.length != 0) {
                    calcRate = rateCalculator.calculateForType("GBP_TRY", usdmid, bids, asks);
                    rateCacheService.saveCalcRate(calcRate);
                    //save to database
                }
            }
            default -> {
                LOGGER.error("Invalid raw rate type: {}", type);
            }
        }
    }

    private void calculateAndSaveUSDMID() {
        List<RawRate> rawRatesOfTypeUSD_TRY = rateCacheService.getAllRawRatesForType("USD_TRY");

        List<Double[]> values = getBidsAndAsks(rawRatesOfTypeUSD_TRY);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        Double usdmid = rateCalculator.calculateUSDMID(bids, asks);
        LOGGER.info("usdmid: {}", usdmid);

        if (usdmid != null) {
            rateCacheService.saveUSDMID(usdmid);
        }
    }

    private List<Double[]> getBidsAndAsks(List<RawRate> rates) {
        List<Double> bids = new ArrayList<>();
        rates.forEach(platformRate -> bids.add(platformRate.getBid()));

        List<Double> asks = new ArrayList<>();
        rates.forEach(platformRate -> asks.add(platformRate.getAsk()));

        return Arrays.asList(bids.toArray(new Double[0]), asks.toArray(new Double[0]));
    }
}
