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

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Implementation of the {@link IRateCalculator} interface that uses
 * a Python script to perform various rate calculations.
 * <p>
 * This calculator loads a Python source file and executes specific
 * Python functions using GraalVM's polyglot API to calculate rates.
 * It supports calculating mean rates, raw rate types, USD/TRY rates,
 * and checks for differences in rates.
 * </p>
 */
public class PythonCalculator implements IRateCalculator {

    private final Logger LOGGER = LogManager.getLogger(PythonCalculator.class);
    private final IRateFactory rateFactory;
    private final IRateConverter rateConverter;
    private final ICalculatorLoader calculatorLoader;

    private Source source;
    private String path;

    /**
     * Constructs a PythonCalculator instance with required dependencies.
     *
     * @param rateFactory      Factory to create rate objects.
     * @param rateConverter    Converter to convert between raw and calculated rate types.
     * @param calculatorLoader Loader responsible for loading the Python source file.
     */
    public PythonCalculator(IRateFactory rateFactory, IRateConverter rateConverter, ICalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.calculatorLoader = calculatorLoader;
        this.rateConverter = rateConverter;
    }

    /**
     * Initializes the calculator by loading the Python source file from the given path.
     *
     * @param calculatorPath Path to the Python calculator script.
     * @throws CalculatorException If the source file cannot be loaded or read.
     */
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

    /**
     * Calculates the mean bid and ask rates using the Python calculator.
     *
     * @param bids Array of bid prices.
     * @param asks Array of ask prices.
     * @return {@link MeanRate} containing the calculated mean bid and ask.
     * @throws CalculatorException If calculation fails.
     */
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

    /**
     * Calculates a {@link CalculatedRate} for a given raw rate type using USD mid rate,
     * bids, and asks via the Python calculator.
     *
     * @param type   Raw rate type identifier.
     * @param usdmid USD mid price.
     * @param bids   Array of bid prices.
     * @param asks   Array of ask prices.
     * @return {@link CalculatedRate} result for the specified rate type.
     * @throws CalculatorException If calculation fails.
     */
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

    /**
     * Calculates the USD/TRY exchange rate using the Python calculator.
     *
     * @param bids Array of bid prices.
     * @param asks Array of ask prices.
     * @return {@link CalculatedRate} for USD/TRY.
     * @throws CalculatorException If calculation fails.
     */
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

    /**
     * Checks whether the incoming raw rate differs from the mean rate by at least 1%.
     *
     * @param incomingRate The raw incoming rate.
     * @param meanRate     The calculated mean rate.
     * @return true if difference is at least 1%, false otherwise.
     * @throws CalculatorException If the calculation fails.
     */
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

    /**
     * Calculates the USD mid price from bids and asks using the Python calculator.
     *
     * @param bids Array of bid prices.
     * @param asks Array of ask prices.
     * @return Calculated USD mid price as a Double.
     * @throws CalculatorException If calculation fails.
     */
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

    /**
     * Returns the strategy identifier for this calculator.
     *
     * @return The string "PYTHON".
     */
    @Override
    public String getStrategy() {
        return "PYTHON";
    }

    /**
     * Returns the path of the loaded Python calculator script.
     *
     * @return Path string of the Python source file.
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * Returns the logger instance used by this class.
     *
     * @return Logger instance.
     */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    /**
     * Sets the path of the Python calculator script.
     *
     * @param path Path string.
     */
    private void setPath(String path) {
        this.path = path;
    }
}
