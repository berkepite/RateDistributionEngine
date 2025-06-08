package com.berkepite.RateDistributionEngine.calculator;

import com.berkepite.RateDistributionEngine.common.calculator.ICalculatorLoader;
import com.berkepite.RateDistributionEngine.common.calculator.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorLoadingException;
import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.MeanRate;
import com.berkepite.RateDistributionEngine.common.rate.IRateConverter;
import com.berkepite.RateDistributionEngine.common.rate.IRateFactory;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.nio.file.Path;
import java.time.Instant;

public class JavascriptCalculator implements IRateCalculator {
    private final Logger LOGGER = LogManager.getLogger(JavascriptCalculator.class);
    private final IRateFactory rateFactory;
    private final IRateConverter rateConverter;
    private final ICalculatorLoader calculatorLoader;
    private Source source;

    private String path;

    public JavascriptCalculator(IRateFactory rateFactory, IRateConverter rateConverter, ICalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.calculatorLoader = calculatorLoader;
        this.rateConverter = rateConverter;
    }

    @Override
    public void init(String calculatorPath) throws CalculatorException {
        setPath(calculatorPath);

        try {
            Path _path = calculatorLoader.load(calculatorPath);
            source = Source.newBuilder("js", _path.toFile()).mimeType("application/javascript+module").build();

        } catch (Exception e) {
            throw new CalculatorLoadingException("Could not load calculator source.", e);
        }
    }

    @Override
    public MeanRate calculateMeanRate(Double[] bids, Double[] asks) throws CalculatorException {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);

            Value calculateMean = module.getMember("calculateMeanRate");
            Value result = calculateMean.execute(bids, asks);

            return rateFactory.createMeanRate(
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble());

        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate mean rate.", e);
        }
    }

    @Override
    public CalculatedRate calculateForRawRateType(String type, Double usdmid, Double[] bids, Double[] asks) throws CalculatorException {
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
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate for raw rate %s.".formatted(type), e);
        }
    }

    @Override
    public CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks) throws CalculatorException {
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
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate for USD_TRY.", e);
        }
    }

    @Override
    public boolean hasAtLeastOnePercentDiff(RawRate incomingRate, MeanRate meanRate) throws CalculatorException {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value hasAtLeastOnePercentDiff = module.getMember("hasAtLeastOnePercentDiff");

            Value result = hasAtLeastOnePercentDiff.execute(
                    incomingRate.getBid(), incomingRate.getAsk(), meanRate.getMeanBid(), meanRate.getMeanAsk());

            return result.asBoolean();
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate one percent difference.", e);
        }
    }

    @Override
    public Double calculateUSDMID(Double[] bids, Double[] asks) throws CalculatorException {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value calculateUSDMID = module.getMember("calculateUSDMID");

            Value result = calculateUSDMID.execute(bids, asks);

            return result.asDouble();
        } catch (Exception e) {
            throw new CalculatorException("Failed to calculate for usdmid.", e);
        }
    }

    @Override
    public String getStrategy() {
        return "JAVASCRIPT";
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    private void setPath(String path) {
        this.path = path;
    }
}
