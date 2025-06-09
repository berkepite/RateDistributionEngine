package com.berkepite.RateDistributionEngine.rate;

import com.berkepite.RateDistributionEngine.common.cache.IRateCacheService;
import com.berkepite.RateDistributionEngine.common.calculator.ICalculatorFactory;
import com.berkepite.RateDistributionEngine.common.calculator.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.exception.cache.CacheException;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.rate.*;
import com.berkepite.RateDistributionEngine.exception.ExceptionHandler;
import com.berkepite.RateDistributionEngine.producer.KafkaCalcRateProducer;
import com.berkepite.RateDistributionEngine.producer.KafkaRawRateProducer;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for managing raw and calculated currency rates.
 * <p>
 * This service handles receiving raw rates, calculating mean and calculated rates,
 * caching them, and producing rate updates to Kafka topics.
 * </p>
 */
@Service
public class RateManager implements IRateManager {
    private final Logger LOGGER = LogManager.getLogger(RateManager.class);

    private final IRatesLoader ratesLoader;
    private final ExceptionHandler exceptionHandler;
    private final IRateCacheService rateCacheService;
    private final ICalculatorFactory calculatorFactory;
    private IRateCalculator rateCalculator;
    private final KafkaRawRateProducer kafkaRawRateProducer;
    private final KafkaCalcRateProducer kafkaCalcRateProducer;

    private List<String> rawRateTypesExcludingUSD_TRY;

    /**
     * Constructs the RateManager with dependencies injected.
     *
     * @param ratesLoader           Loader service for raw rate types configuration.
     * @param rateCacheService      Cache service for raw and calculated rates.
     * @param calculatorFactory     Factory to get the rate calculator implementation.
     * @param kafkaRawRateProducer  Producer service for raw rate events to Kafka.
     * @param kafkaCalcRateProducer Producer service for calculated rate events to Kafka.
     * @param exceptionHandler      Centralized exception handler for rate-related exceptions.
     */
    public RateManager(IRatesLoader ratesLoader, IRateCacheService rateCacheService,
                       ICalculatorFactory calculatorFactory, KafkaRawRateProducer kafkaRawRateProducer,
                       KafkaCalcRateProducer kafkaCalcRateProducer, ExceptionHandler exceptionHandler) {
        this.rateCacheService = rateCacheService;
        this.calculatorFactory = calculatorFactory;
        this.ratesLoader = ratesLoader;
        this.kafkaRawRateProducer = kafkaRawRateProducer;
        this.kafkaCalcRateProducer = kafkaCalcRateProducer;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Initializes the rate calculator and prepares the list of raw rate types
     * excluding the "USD_TRY" pair after the bean construction.
     */
    @PostConstruct
    public void init() {
        try {
            rateCalculator = calculatorFactory.getCalculator();
        } catch (CalculatorException e) {
            exceptionHandler.handle(e);
            LOGGER.error("Failed to initialize rate calculator. Aborting initialization.");
            return;
        }

        rawRateTypesExcludingUSD_TRY = ratesLoader.getRatesList();
        rawRateTypesExcludingUSD_TRY.remove("USD_TRY");
    }

    /**
     * Processes an incoming raw rate by performing the following steps:
     * <ul>
     *     <li>Sends the raw rate to Kafka for persistence.</li>
     *     <li>Checks if the raw rate exists in cache and saves it if new or updated with
     *     a significant difference compared to cached mean rates.</li>
     *     <li>If the rate type is "USD_TRY", calculates and saves the USD mid rate and related rates.</li>
     *     <li>Otherwise, calculates and saves rates based on the incoming raw rate's type.</li>
     * </ul>
     *
     * @param incomingRate The raw rate received to be managed.
     */
    public void manageIncomingRawRate(RawRate incomingRate) {
        kafkaRawRateProducer.sendRawRate(incomingRate);

        try {
            if (rateCacheService.getRawRate(incomingRate) == null) {
                rateCacheService.saveRawRate(incomingRate);
            } else {
                var allRawRatesForType = rateCacheService.getAllRawRatesForType(incomingRate.getType());

                if (allRawRatesForType.isEmpty()) {
                    LOGGER.error("No cached rates found for type {} while calculating mean rate. Aborting.",
                            incomingRate.getType());
                    return;
                }

                var values = getBidsAndAsks(allRawRatesForType);
                var bids = values.get(0);
                var asks = values.get(1);

                var meanRate = rateCalculator.calculateMeanRate(bids, asks);
                if (!rateCalculator.hasAtLeastOnePercentDiff(incomingRate, meanRate)) {
                    rateCacheService.saveRawRate(incomingRate);
                } else {
                    LOGGER.info("Incoming rate from provider {} for type {} differs more than 1%, dropping rate.",
                            incomingRate.getProvider(), incomingRate.getType());
                    return;
                }
            }

            if (incomingRate.getType().equals("USD_TRY")) {
                calculateAndSaveUSDMID(incomingRate);
                calculateAndSaveForUSD_TRY(incomingRate);

                if (!rawRateTypesExcludingUSD_TRY.isEmpty()) {
                    rawRateTypesExcludingUSD_TRY.forEach(this::calculateAndSaveForType);
                }
            } else {
                calculateAndSaveForType(incomingRate.getType());
            }

        } catch (CalculatorException e) {
            exceptionHandler.handle(e, rateCalculator);
        } catch (CacheException e) {
            exceptionHandler.handle(e, rateCacheService);
        }
    }

    /**
     * Calculates and persists the calculated rate for a given raw rate type.
     * Also sends the calculated rate event to Kafka.
     *
     * @param type The raw rate type to calculate for.
     */
    private synchronized void calculateAndSaveForType(String type) {
        try {
            Double usdmid = rateCacheService.getUSDMID();
            if (usdmid == null) {
                LOGGER.warn("USD mid rate not found in cache. Aborting calculation for type: {}.", type);
                return;
            }

            List<RawRate> allRawRates = rateCacheService.getAllRawRatesForType(type);
            if (allRawRates.isEmpty()) {
                LOGGER.error("No raw rates cached for type {}. Aborting calculation.", type);
                return;
            }

            var values = getBidsAndAsks(allRawRates);
            var bids = values.get(0);
            var asks = values.get(1);

            CalculatedRate calcRate = rateCalculator.calculateForRawRateType(type, usdmid, bids, asks);
            rateCacheService.saveCalcRate(calcRate);
            kafkaCalcRateProducer.sendCalcRate(calcRate);

        } catch (CalculatorException e) {
            exceptionHandler.handle(e, rateCalculator);
        } catch (CacheException e) {
            exceptionHandler.handle(e, rateCacheService);
        }
    }

    /**
     * Calculates and saves the calculated rate specifically for "USD_TRY" raw rate type.
     *
     * @param incomingRate The incoming raw rate for "USD_TRY".
     */
    private synchronized void calculateAndSaveForUSD_TRY(RawRate incomingRate) {
        try {
            var allRawRatesForUSD_TRY = rateCacheService.getAllRawRatesForType(incomingRate.getType());

            if (allRawRatesForUSD_TRY.isEmpty()) {
                LOGGER.error("No cached rates found for USD_TRY type. Aborting calculation.");
                return;
            }

            var values = getBidsAndAsks(allRawRatesForUSD_TRY);
            var bids = values.get(0);
            var asks = values.get(1);

            CalculatedRate calculatedRate = rateCalculator.calculateForUSD_TRY(bids, asks);
            rateCacheService.saveCalcRate(calculatedRate);
            kafkaCalcRateProducer.sendCalcRate(calculatedRate);

        } catch (CalculatorException e) {
            exceptionHandler.handle(e, rateCalculator);
        } catch (CacheException e) {
            exceptionHandler.handle(e, rateCacheService);
        }
    }

    /**
     * Calculates the USD mid rate and saves it in the cache.
     * If the USD mid rate is already cached, recalculates it using all "USD_TRY" raw rates.
     *
     * @param incomingRate The incoming raw rate to calculate from.
     */
    private synchronized void calculateAndSaveUSDMID(RawRate incomingRate) {
        try {
            if (rateCacheService.getUSDMID() == null) {
                Double usdmid = rateCalculator.calculateUSDMID(
                        new Double[]{incomingRate.getBid()},
                        new Double[]{incomingRate.getAsk()});
                LOGGER.info("Calculated new USDMID from scratch: {}", usdmid);
                rateCacheService.saveUSDMID(usdmid);
            } else {
                List<RawRate> allUSDTRY = rateCacheService.getAllRawRatesForType("USD_TRY");
                if (allUSDTRY.isEmpty()) {
                    LOGGER.error("No cached USD_TRY raw rates found while calculating USDMID.");
                    return;
                }

                var values = getBidsAndAsks(allUSDTRY);
                var bids = values.get(0);
                var asks = values.get(1);

                Double calculated_usdmid = rateCalculator.calculateUSDMID(bids, asks);
                LOGGER.info("Recalculated USDMID: {}", calculated_usdmid);
                rateCacheService.saveUSDMID(calculated_usdmid);
            }
        } catch (CalculatorException e) {
            exceptionHandler.handle(e, rateCalculator);
        } catch (CacheException e) {
            exceptionHandler.handle(e, rateCacheService);
        }
    }

    /**
     * Extracts bids and asks from a list of raw rates.
     *
     * @param rates List of raw rates.
     * @return A list containing two arrays: first for bids, second for asks.
     */
    private List<Double[]> getBidsAndAsks(List<RawRate> rates) {
        if (rates == null || rates.isEmpty()) {
            return null;
        }

        List<Double> bids = new ArrayList<>();
        rates.forEach(rate -> bids.add(rate.getBid()));

        List<Double> asks = new ArrayList<>();
        rates.forEach(rate -> asks.add(rate.getAsk()));

        return Arrays.asList(bids.toArray(new Double[0]), asks.toArray(new Double[0]));
    }
}
