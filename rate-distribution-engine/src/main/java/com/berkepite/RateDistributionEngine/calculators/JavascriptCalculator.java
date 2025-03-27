package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.rates.RateConverter;
import com.berkepite.RateDistributionEngine.rates.RateFactory;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;

public class JavascriptCalculator implements IRateCalculator {
    private final RateFactory rateFactory;
    private final RateConverter rateConverter;
    private final CalculatorLoader calculatorLoader;
    private static final Logger LOGGER = LogManager.getLogger(JavascriptCalculator.class);
    private Source source;

    public JavascriptCalculator(RateFactory rateFactory, RateConverter rateConverter, CalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.calculatorLoader = calculatorLoader;
        this.rateConverter = rateConverter;
        init();
    }

    public void init() {
        try {
            Reader calculatorSource = calculatorLoader.load("rate_calculators/JAVASCRIPT_CALCULATOR.mjs");

            source = Source.newBuilder("js", calculatorSource, "jsmod.js").mimeType("application/javascript+module").build();
        } catch (IOException e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public RawRate calculateMeanRate(RawRate incomingRate, Double[] bids, Double[] asks) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);

            Value calculateMean = module.getMember("calculateMeanRate");
            Value result = calculateMean.execute(bids, asks);

            return rateFactory.createRawRate(incomingRate.getType(),
                    incomingRate.getProvider(),
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble(),
                    incomingRate.getTimestamp());
        }
    }

    @Override
    public CalculatedRate calculateForRawRateType(String type, Double usdmid, Double[] bids, Double[] asks) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);

            Value m_calculateForRawRateType = module.getMember("calculateForRawRateType");
            Value result = m_calculateForRawRateType.execute(usdmid, bids, asks);

            String calcRateType = rateConverter.convertFromRawToCalc(type);

            return rateFactory.createCalcRate(calcRateType,
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble(),
                    Instant.now());
        }
    }

    @Override
    public CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);

            Value m_calculateForUSD_TRY = module.getMember("calculateForUSD_TRY");
            Value result = m_calculateForUSD_TRY.execute(bids, asks);

            String calcRateType = rateConverter.convertFromRawToCalc("USD_TRY");

            return rateFactory.createCalcRate(calcRateType,
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble(),
                    Instant.now());
        }
    }

    @Override
    public boolean hasAtLeastOnePercentDiff(RawRate incomingRate, RawRate meanRate) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value hasAtLeastOnePercentDiff = module.getMember("hasAtLeastOnePercentDiff");

            Value result = hasAtLeastOnePercentDiff.execute(
                    incomingRate.getBid(), incomingRate.getAsk(), meanRate.getBid(), meanRate.getAsk());

            return result.asBoolean();
        }
    }

    @Override
    public Double calculateUSDMID(Double[] bids, Double[] asks) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value calculateUSDMID = module.getMember("calculateUSDMID");

            Value result = calculateUSDMID.execute(bids, asks);

            return result.asDouble();
        }
    }
}
