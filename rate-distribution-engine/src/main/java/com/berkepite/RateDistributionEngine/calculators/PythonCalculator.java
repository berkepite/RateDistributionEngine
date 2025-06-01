package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.common.calculators.ICalculatorLoader;
import com.berkepite.RateDistributionEngine.common.calculators.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorLoadingException;
import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rates.MeanRate;
import com.berkepite.RateDistributionEngine.common.rates.IRateConverter;
import com.berkepite.RateDistributionEngine.common.rates.IRateFactory;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

public class PythonCalculator implements IRateCalculator {
    private final IRateFactory rateFactory;
    private final IRateConverter rateConverter;
    private final ICalculatorLoader calculatorLoader;

    private Source source;
    private String path;

    public PythonCalculator(IRateFactory rateFactory, IRateConverter rateConverter, ICalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.calculatorLoader = calculatorLoader;
        this.rateConverter = rateConverter;
    }

    @Override
    public void init(String calculatorPath) throws CalculatorException {
        setPath(calculatorPath);

        try {
            Path _path = calculatorLoader.load(calculatorPath);
            source = Source.newBuilder("python", _path.toFile()).build();

        } catch (IOException e) {
            throw new CalculatorLoadingException("Could not load calculator source.", e);
        }
    }

    @Override
    public MeanRate calculateMeanRate(Double[] bids, Double[] asks) throws CalculatorException {
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
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate mean rate.", e);
        }
    }

    @Override
    public CalculatedRate calculateForRawRateType(String type, Double usdmid, Double[] bids, Double[] asks) throws CalculatorException {
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
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate for raw rate %s.".formatted(type), e);
        }
    }

    @Override
    public CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks) throws CalculatorException {
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
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate for USD_TRY.", e);
        }
    }


    @Override
    public boolean hasAtLeastOnePercentDiff(RawRate incomingRate, MeanRate meanRate) throws CalculatorException {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);
            Value m_hasAtLeastOnePercentDiff = module.getMember("has_at_least_one_percent_diff");

            Value result = m_hasAtLeastOnePercentDiff.execute(
                    incomingRate.getBid(), incomingRate.getAsk(), meanRate.getMeanBid(), meanRate.getMeanAsk());

            return result.asBoolean();
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate one percent difference.", e);
        }
    }

    @Override
    public Double calculateUSDMID(Double[] bids, Double[] asks) throws CalculatorException {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);
            Value calculateUSDMID = module.getMember("calculate_usdmid");

            Value result = calculateUSDMID.execute(bids, asks);

            return result.asDouble();
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate for usdmid.", e);
        }
    }

    @Override
    public String getStrategy() {
        return "PYTHON";
    }

    @Override
    public String getPath() {
        return path;
    }

    private void setPath(String path) {
        this.path = path;
    }
}
