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

/**
 * Javascript-based implementation of {@link IRateCalculator} that evaluates
 * currency rate calculations using a JavaScript module executed within GraalVM.
 * <p>
 * This class loads the JavaScript source file dynamically, and provides
 * methods to calculate mean rates, specific raw rate types, USD_TRY rates,
 * and USD mid rate, as well as a method to check if there is at least a
 * one percent difference between given rates.
 * </p>
 */
public class JavascriptCalculator implements IRateCalculator {

    private final Logger LOGGER = LogManager.getLogger(JavascriptCalculator.class);
    private final IRateFactory rateFactory;
    private final IRateConverter rateConverter;
    private final ICalculatorLoader calculatorLoader;
    private Source source;
    private String path;

    /**
     * Constructs a JavascriptCalculator with the required dependencies.
     *
     * @param rateFactory      factory for creating rate objects
     * @param rateConverter    converter for rate type conversions
     * @param calculatorLoader loader to load the JavaScript calculator file
     */
    public JavascriptCalculator(IRateFactory rateFactory, IRateConverter rateConverter, ICalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.calculatorLoader = calculatorLoader;
        this.rateConverter = rateConverter;
    }

    /**
     * Initializes the calculator by loading the JavaScript source file
     * from the given path.
     *
     * @param calculatorPath path to the JavaScript calculator file
     * @throws CalculatorException if the source file cannot be loaded
     */
    @Override
    public void init(String calculatorPath) throws CalculatorException {
        setPath(calculatorPath);
        try {
            Path _path = calculatorLoader.load(calculatorPath);
            source = Source.newBuilder("js", _path.toFile())
                    .mimeType("application/javascript+module")
                    .build();
        } catch (Exception e) {
            throw new CalculatorLoadingException("Could not load calculator source.", e);
        }
    }

    /**
     * Calculates the mean bid and ask rates from the provided bid and ask arrays
     * by invoking the corresponding JavaScript function.
     *
     * @param bids array of bid prices
     * @param asks array of ask prices
     * @return the calculated {@link MeanRate}
     * @throws CalculatorException if the calculation fails
     */
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

    /**
     * Calculates a rate for a specified raw rate type using the USD mid price,
     * bids, and asks by invoking the corresponding JavaScript function.
     *
     * @param type   the raw rate type identifier
     * @param usdmid the USD mid price
     * @param bids   array of bid prices
     * @param asks   array of ask prices
     * @return the calculated {@link CalculatedRate}
     * @throws CalculatorException if the calculation fails
     */
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

    /**
     * Calculates the USD_TRY rate based on the given bids and asks
     * by invoking the corresponding JavaScript function.
     *
     * @param bids array of bid prices
     * @param asks array of ask prices
     * @return the calculated {@link CalculatedRate}
     * @throws CalculatorException if the calculation fails
     */
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

    /**
     * Determines whether there is at least a one percent difference between
     * the incoming raw rate and the mean rate, by invoking the corresponding
     * JavaScript function.
     *
     * @param incomingRate the incoming raw rate
     * @param meanRate     the mean rate for comparison
     * @return true if the difference is at least one percent; false otherwise
     * @throws CalculatorException if the check fails
     */
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

    /**
     * Calculates the USD mid rate based on the given bids and asks
     * by invoking the corresponding JavaScript function.
     *
     * @param bids array of bid prices
     * @param asks array of ask prices
     * @return the USD mid rate value
     * @throws CalculatorException if the calculation fails
     */
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

    /**
     * Returns the strategy name for this calculator implementation.
     *
     * @return the string "JAVASCRIPT"
     */
    @Override
    public String getStrategy() {
        return "JAVASCRIPT";
    }

    /**
     * Returns the path to the loaded JavaScript calculator file.
     *
     * @return the file path as a string
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * Returns the logger used by this class.
     *
     * @return the {@link Logger} instance
     */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    /**
     * Sets the path of the JavaScript calculator file.
     *
     * @param path the file path as a string
     */
    private void setPath(String path) {
        this.path = path;
    }
}
