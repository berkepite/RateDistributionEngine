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
public class RateManager {
    private final Logger LOGGER = LogManager.getLogger(RateManager.class);

    private IRateCalculator rateCalculator;
    private final CalculatorFactory calculatorFactory;
    private final IRateCacheService rateCacheService;
    private final KafkaRawRateProducer kafkaRawRateProducer;
    private final KafkaCalcRateProducer kafkaCalcRateProducer;

    @Value("${app.rate-calculation-strategy}")
    private CalculatorEnum rateCalculationStrategy;

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
        this.kafkaRawRateProducer = kafkaRawRateProducer;
        this.kafkaCalcRateProducer = kafkaCalcRateProducer;
    }

    /**
     * Initializes the rate calculator based on the configured strategy.
     */
    @PostConstruct
    public void init() {
        rateCalculator = calculatorFactory.getCalculator(rateCalculationStrategy);
    }

    /**
     * Manages an incoming raw rate by calculating the mean and checking for significant differences.
     * If the difference is below a threshold, the raw rate is saved and used to calculate other rates.
     *
     * @param incomingRate The incoming raw rate to manage.
     */
    public void manageRawRate(RawRate incomingRate) {
        // write to raw database
        kafkaRawRateProducer.sendRawRate(incomingRate);
        List<RawRate> allRawRatesForType = rateCacheService.getAllRawRatesForType(incomingRate.getType());

        List<Double[]> values = getBidsAndAsks(allRawRatesForType);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        // If no bids or asks are provided then it returns incomingRate.
        // So the if condition below returns true.
        if (bids == null || asks == null) {
            // TODO alert
            return;
        }
        RawRate meanRate = rateCalculator.calculateMeanRate(incomingRate, bids, asks);
        // intermediate rate class maybe?

        // If does not have at least %1 difference, save the raw rate and calculate appropriate rates
        if (!rateCalculator.hasAtLeastOnePercentDiff(incomingRate, meanRate)) {
            rateCacheService.saveRawRate(incomingRate);

//            if (incomingRate.getType().equals("USD_TRY")) {
//                calculateAndSaveUSDMID();
//                calculateAndSaveForUSD_TRY();
//
//                Arrays.stream(RawRateEnum.values())
//                        .filter(val -> val != "USD_TRY")
//                        .forEach(
//                                val -> calculateAndSaveForType(val.toString())
//                        );
//
//            } else {
//                calculateAndSaveForType(incomingRate.getType());
//            }
        }
    }

    /**
     * Calculates and saves the calculated rate for the specified type.
     *
     * @param type The raw rate type to calculate and save.
     */
    private void calculateAndSaveForType(String type) {
        Double usdmid = rateCacheService.getUSDMID();
        List<RawRate> allRawRates = rateCacheService.getAllRawRatesForType(type);

        List<Double[]> values = getBidsAndAsks(allRawRates);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);


        // Check if usdmid, bids, and asks are valid before calculating
        if (usdmid != null && asks.length != 0 && bids.length != 0) {
            CalculatedRate calcRate = rateCalculator.calculateForRawRateType(type, usdmid, bids, asks);
            rateCacheService.saveCalcRate(calcRate);
            // Save to Kafka
            kafkaCalcRateProducer.sendCalcRate(calcRate);
        } else {
            LOGGER.debug("Bids and/or asks are empty or usdmid is null!\nusdmid: {} bids: {} asks: {}", usdmid, bids, asks);
        }
    }

    /**
     * Calculates and saves the USD mid rate based on the USD/TRY rates.
     */
    private void calculateAndSaveUSDMID() {
        List<RawRate> rawRatesOfTypeUSD_TRY = rateCacheService.getAllRawRatesForType("USD_TRY");
        List<Double[]> bidsAndAsks = getBidsAndAsks(rawRatesOfTypeUSD_TRY);

        if (bidsAndAsks == null) {
            LOGGER.warn("There is no USD_TRY values in cache for calculating USDMID! Aborting...");
            return;
        }

        Double[] bids = bidsAndAsks.get(0);
        Double[] asks = bidsAndAsks.get(1);

        Double usdmid = rateCalculator.calculateUSDMID(bids, asks);
        rateCacheService.saveUSDMID(usdmid);
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
