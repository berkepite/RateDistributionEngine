package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.cache.IRateCacheService;
import com.berkepite.MainApplication32Bit.calculators.CalculatorFactory;
import com.berkepite.MainApplication32Bit.calculators.IRateCalculator;
import com.berkepite.MainApplication32Bit.producers.KafkaCalcRateProducer;
import com.berkepite.MainApplication32Bit.producers.KafkaRawRateProducer;
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
public class RateService {
    private final Logger LOGGER = LogManager.getLogger(RateService.class);

    private IRateCalculator rateCalculator;
    private final CalculatorFactory calculatorFactory;
    private final IRateCacheService rateCacheService;
    private final KafkaRawRateProducer kafkaRawRateProducer;
    private final KafkaCalcRateProducer kafkaCalcRateProducer;

    @Value("${app.rate-calculation-strategy}")
    private String rateCalculationStrategy;
    @Value("${app.rate-calculation-source-path}")
    private String rateCalculationSourcePath;

    /**
     * Constructor for RateService.
     *
     * @param rateCacheService      Service for caching raw and calculated rates.
     * @param calculatorFactory     Factory to create rate calculators.
     * @param kafkaRawRateProducer  Producer for raw rates to Kafka.
     * @param kafkaCalcRateProducer Producer for calculated rates to Kafka.
     */
    public RateService(IRateCacheService rateCacheService, CalculatorFactory calculatorFactory, KafkaRawRateProducer kafkaRawRateProducer, KafkaCalcRateProducer kafkaCalcRateProducer) {
        this.rateCacheService = rateCacheService;
        this.calculatorFactory = calculatorFactory;
        this.kafkaRawRateProducer = kafkaRawRateProducer;
        this.kafkaCalcRateProducer = kafkaCalcRateProducer;
    }

    /**
     * Initializes the rate calculator based on the configured strategy and source path.
     */
    @PostConstruct
    public void init() {
        rateCalculator = calculatorFactory.getCalculator(rateCalculationStrategy, rateCalculationSourcePath);
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

        RawRate meanRawRate = rateCalculator.calculateMeansOfRawRates(incomingRate, bids, asks);

        // If does not have at least %1 difference, save the raw rate and calculate appropriate rates
        if (!rateCalculator.hasAtLeastOnePercentDiff(meanRawRate.getBid(), meanRawRate.getAsk(), incomingRate.getBid(), incomingRate.getAsk())) {
            rateCacheService.saveRawRate(incomingRate);

            if (incomingRate.getType().equals(RawRateEnum.USD_TRY.toString())) {
                calculateAndSaveUSDMID();
                calculateAndSaveForUSD_TRY();

                Arrays.stream(RawRateEnum.values())
                        .filter(val -> !val.toString().equals("USD_TRY"))
                        .forEach(
                                val -> calculateAndSaveForType(val.toString())
                        );

            } else {
                calculateAndSaveForType(incomingRate.getType());
            }
        }
    }

    /**
     * Calculates and saves the rate for USD/TRY based on the cached raw rates.
     */
    private void calculateAndSaveForUSD_TRY() {
        List<RawRate> allRawRates = rateCacheService.getAllRawRatesForType(RawRateEnum.USD_TRY.toString());
        List<Double[]> values = getBidsAndAsks(allRawRates);

        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        // Check if bids and asks are valid before calculating
        if (asks.length != 0 && bids.length != 0) {
            CalculatedRate calcRate = rateCalculator.calculateForUSD_TRY(bids, asks);
            rateCacheService.saveCalcRate(calcRate);
            // Save to Kafka
            kafkaCalcRateProducer.sendCalcRate(calcRate);
        } else {
            LOGGER.debug("Bids and/or asks are empty!\nbids: {} asks: {}", bids, asks);
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

        String calcRateType = mapRawRateTypeToCalcRateType(type);
        if (calcRateType == null) return;

        // Check if usdmid, bids, and asks are valid before calculating
        if (usdmid != null && asks.length != 0 && bids.length != 0) {
            CalculatedRate calcRate = rateCalculator.calculateForType(calcRateType, usdmid, bids, asks);
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

        List<Double[]> values = getBidsAndAsks(rawRatesOfTypeUSD_TRY);
        Double[] bids = values.get(0);
        Double[] asks = values.get(1);

        Double usdmid = rateCalculator.calculateUSDMID(bids, asks);
        LOGGER.info("usdmid: {}", usdmid);

        if (usdmid != null) {
            rateCacheService.saveUSDMID(usdmid);
        }
    }

    /**
     * Retrieves the bids and asks from a list of raw rates.
     *
     * @param rates The list of raw rates to extract bids and asks from.
     * @return A list containing two arrays: bids and asks.
     */
    private List<Double[]> getBidsAndAsks(List<RawRate> rates) {
        List<Double> bids = new ArrayList<>();
        rates.forEach(platformRate -> bids.add(platformRate.getBid()));

        List<Double> asks = new ArrayList<>();
        rates.forEach(platformRate -> asks.add(platformRate.getAsk()));

        return Arrays.asList(bids.toArray(new Double[0]), asks.toArray(new Double[0]));
    }

    /**
     * Maps a raw rate type to its corresponding calculated rate type.
     *
     * @param type The raw rate type to map.
     * @return The corresponding calculated rate type.
     */
    private String mapRawRateTypeToCalcRateType(String type) {
        String significantHalf = type.substring(0, 4);

        List<CalculatedRateEnum> list = Arrays.stream(CalculatedRateEnum.values())
                .filter(val -> val.toString().substring(0, 4).equals(significantHalf))
                .toList();

        if (list.getFirst() != null) {
            return list.getFirst().toString();
        } else {
            Exception ex = new IllegalArgumentException("Unknown raw rate type: " + type);
            LOGGER.error(ex);
            return null;
        }
    }
}
