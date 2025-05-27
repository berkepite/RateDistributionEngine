package com.berkepite.RateDistributionEngine.rates;

import com.berkepite.RateDistributionEngine.cache.IRateCacheService;
import com.berkepite.RateDistributionEngine.calculators.CalculatorEnum;
import com.berkepite.RateDistributionEngine.calculators.CalculatorFactory;
import com.berkepite.RateDistributionEngine.calculators.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.berkepite.RateDistributionEngine.producers.KafkaCalcRateProducer;
import com.berkepite.RateDistributionEngine.producers.KafkaRawRateProducer;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for managing raw and calculated rates, including rate calculation, caching and database saving.
 */
@Service
public class RateManager implements IRateManager {
    private final Logger LOGGER = LogManager.getLogger(RateManager.class);

    private IRateCalculator rateCalculator;
    private final CalculatorFactory calculatorFactory;
    private final IRateCacheService rateCacheService;
    //private final KafkaRawRateProducer kafkaRawRateProducer;
    //private final KafkaCalcRateProducer kafkaCalcRateProducer;

    @Value("${app.rate-calculation-strategy}")
    private CalculatorEnum rateCalculationStrategy;

    @Value("${app.rate-calculator-path}")
    private String rateCalculatorPath;

    @Value("#{'${app.coordinator.rates}'.split(',')}")
    private List<String> rawRateTypesExcludingUSD_TRY;

    /**
     * Constructor for RateService.
     *
     * @param rateCacheService      Service for caching raw and calculated rates.
     * @param calculatorFactory     Factory to create rate calculators.
     * @param kafkaRawRateProducer  Producer for raw rates to Kafka.
     * @param kafkaCalcRateProducer Producer for calculated rates to Kafka.
     */
    public RateManager(IRateCacheService rateCacheService, CalculatorFactory calculatorFactory, KafkaRawRateProducer kafkaRawRateProducer, KafkaCalcRateProducer kafkaCalcRateProducer) {
        this.rateCacheService = rateCacheService;
        this.calculatorFactory = calculatorFactory;
        // this.kafkaRawRateProducer = kafkaRawRateProducer;
        // this.kafkaCalcRateProducer = kafkaCalcRateProducer;
    }

    /**
     * Initializes the rate calculator based on the configured strategy.
     */
    @PostConstruct
    public void init() {
        rateCalculator = calculatorFactory.getCalculator(rateCalculationStrategy, rateCalculatorPath);

        rawRateTypesExcludingUSD_TRY.remove("USD_TRY");
    }

    /**
     * Manages an incoming raw rate by calculating the mean and checking for significant differences.
     * If the difference is below a threshold, the raw rate is saved and used to calculate other rates.
     *
     * @param incomingRate The incoming raw rate to manage.
     */
    public void manageIncomingRawRate(RawRate incomingRate) {
        // write to raw database
        //kafkaRawRateProducer.sendRawRate(incomingRate);

        if (rateCacheService.getRawRate(incomingRate) == null) {
            rateCacheService.saveRawRate(incomingRate);
        } else {
            var allRawRatesForType = rateCacheService.getAllRawRatesForType(incomingRate.getType());

            if (allRawRatesForType.isEmpty()) {
                LOGGER.error("No rates found in cache for type: {}.", incomingRate.getType());
                return;
            }

            var values = getBidsAndAsks(allRawRatesForType);
            var bids = values.get(0);
            var asks = values.get(1);

            var meanRate = rateCalculator.calculateMeanRate(bids, asks);
            if (!rateCalculator.hasAtLeastOnePercentDiff(incomingRate, meanRate)) {
                rateCacheService.saveRawRate(incomingRate);
            } else return;
        }

        if (incomingRate.getType().equals("USD_TRY")) {
            calculateAndSaveUSDMID(incomingRate);
            calculateAndSaveForUSD_TRY(incomingRate);

            rawRateTypesExcludingUSD_TRY.forEach(this::calculateAndSaveForType);

        } else {
            calculateAndSaveForType(incomingRate.getType());
        }

    }

    /**
     * Calculates and saves the calculated rate for the specified type.
     *
     * @param type The raw rate type to calculate and save.
     */
    private synchronized void calculateAndSaveForType(String type) {
        Double usdmid = rateCacheService.getUSDMID();
        if (usdmid == null) {
            LOGGER.warn("No usdmid found in cache while calculating for type: {}. Aborting...", type);
            return;
        }

        List<RawRate> allRawRates = rateCacheService.getAllRawRatesForType(type);

        if (allRawRates.isEmpty()) {
            LOGGER.error("No raw rates found in cache while calculating for type {}. Aborting...", type);
            return;
        }

        var values = getBidsAndAsks(allRawRates);
        var bids = values.get(0);
        var asks = values.get(1);

        CalculatedRate calcRate = rateCalculator.calculateForRawRateType(type, usdmid, bids, asks);

        rateCacheService.saveCalcRate(calcRate);
        //kafkaCalcRateProducer.sendCalcRate(calcRate);
    }

    private synchronized void calculateAndSaveForUSD_TRY(RawRate incomingRate) {
        var allRawRatesForUSD_TRY = rateCacheService.getAllRawRatesForType(incomingRate.getType());

        if (allRawRatesForUSD_TRY.isEmpty()) {
            LOGGER.error("No rates found in cache for type: {}. While calculating for USD_TRY", incomingRate.getType());
            return;
        }

        var values = getBidsAndAsks(allRawRatesForUSD_TRY);
        var bids = values.get(0);
        var asks = values.get(1);

        CalculatedRate calculatedRate = rateCalculator.calculateForUSD_TRY(bids, asks);

        rateCacheService.saveCalcRate(calculatedRate);
        // kafkaCalcRateProducer.sendCalcRate(calculatedRate);
    }


    private synchronized void calculateAndSaveUSDMID(RawRate incomingRate) {
        if (rateCacheService.getUSDMID() == null) {
            Double usdmid = rateCalculator.calculateUSDMID(new Double[]{incomingRate.getBid()}, new Double[]{incomingRate.getAsk()});
            LOGGER.info("Calculated new USDMID from scratch: {}", usdmid);

            rateCacheService.saveUSDMID(usdmid);
        } else {
            List<RawRate> allUSDTRY = rateCacheService.getAllRawRatesForType("USD_TRY");

            if (allUSDTRY.isEmpty()) {
                LOGGER.error("No raw rates found in cache for USD_TRY while calculating usdmid.");
                return;
            }

            var values = getBidsAndAsks(allUSDTRY);
            var bids = values.get(0);
            var asks = values.get(1);

            Double calculated_usdmid = rateCalculator.calculateUSDMID(bids, asks);
            LOGGER.info("Calculated new USDMID: {}", calculated_usdmid);
            rateCacheService.saveUSDMID(calculated_usdmid);
        }
    }


    /**
     * Retrieves the bids and asks from a list of raw rates.
     *
     * @param rates The list of raw rates to extract bids and asks from.
     * @return A list containing two arrays: bids and asks.
     */
    private List<Double[]> getBidsAndAsks(List<RawRate> rates) {
        if (rates == null || rates.isEmpty()) {
            return null;
        }

        List<Double> bids = new ArrayList<>();
        rates.forEach(platformRate -> bids.add(platformRate.getBid()));

        List<Double> asks = new ArrayList<>();
        rates.forEach(platformRate -> asks.add(platformRate.getAsk()));

        return Arrays.asList(bids.toArray(new Double[0]), asks.toArray(new Double[0]));
    }
}
