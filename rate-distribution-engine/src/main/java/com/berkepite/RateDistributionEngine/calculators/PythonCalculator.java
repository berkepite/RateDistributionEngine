package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.MeanRate;
import com.berkepite.RateDistributionEngine.rates.RateConverter;
import com.berkepite.RateDistributionEngine.rates.RateFactory;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;

public class PythonCalculator implements IRateCalculator {
    private final RateFactory rateFactory;
    private final RateConverter rateConverter;
    private final CalculatorLoader calculatorLoader;
    private static final Logger LOGGER = LogManager.getLogger(PythonCalculator.class);
    private Source source;

    public PythonCalculator(RateFactory rateFactory, RateConverter rateConverter, CalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.calculatorLoader = calculatorLoader;
        this.rateConverter = rateConverter;
        init();
    }

    public void init() {
        try {
            Reader calculatorSource = calculatorLoader.load("rate_calculators/PYTHON_CALCULATOR.py");

            source = Source.newBuilder("python", calculatorSource, "pymod.py").build();
        } catch (IOException e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public MeanRate calculateMeanRate(Double[] bids, Double[] asks) {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);

            Value m_calculateMeanRate = module.getMember("calculate_mean_rate");
            Value result = m_calculateMeanRate.execute(bids, asks);

            return rateFactory.createMeanRate(
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble()
            );
        }
    }

    @Override
    public CalculatedRate calculateForRawRateType(String type, Double usdmid, Double[] bids, Double[] asks) {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);

            Value calculateForType = module.getMember("calculate_for_raw_rate_type");
            Value result = calculateForType.execute(usdmid, bids, asks);

            return rateFactory.createCalcRate(type,
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble(),
                    Instant.now());
        }
    }

    @Override
    public CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks) {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);

            Value m_calculateForUSD_TRY = module.getMember("calculate_for_USD_TRY");
            Value result = m_calculateForUSD_TRY.execute(bids, asks);

            String calcRateType = rateConverter.convertFromRawToCalc("USD_TRY");

            return rateFactory.createCalcRate(calcRateType,
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble(),
                    Instant.now());
        }
    }


    @Override
    public boolean hasAtLeastOnePercentDiff(RawRate incomingRate, MeanRate meanRate) {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);
            Value m_hasAtLeastOnePercentDiff = module.getMember("has_at_least_one_percent_diff");

            Value result = m_hasAtLeastOnePercentDiff.execute(
                    incomingRate.getBid(), incomingRate.getAsk(), meanRate.getMeanBid(), meanRate.getMeanAsk());

            return result.asBoolean();
        }
    }

    @Override
    public Double calculateUSDMID(Double[] bids, Double[] asks) {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);
            Value calculateUSDMID = module.getMember("calculate_usdmid");

            Value result = calculateUSDMID.execute(bids, asks);

            return result.asDouble();
        }
    }
}
