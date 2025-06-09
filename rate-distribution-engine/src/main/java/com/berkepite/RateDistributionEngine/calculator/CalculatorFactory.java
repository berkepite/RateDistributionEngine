package com.berkepite.RateDistributionEngine.calculator;

import com.berkepite.RateDistributionEngine.common.calculator.CalculatorEnum;
import com.berkepite.RateDistributionEngine.common.calculator.ICalculatorFactory;
import com.berkepite.RateDistributionEngine.common.calculator.ICalculatorLoader;
import com.berkepite.RateDistributionEngine.common.calculator.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorLoadingException;
import com.berkepite.RateDistributionEngine.common.rate.IRateConverter;
import com.berkepite.RateDistributionEngine.common.rate.IRateFactory;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Factory service responsible for creating and providing the appropriate {@link IRateCalculator}
 * implementation based on the configured rate calculation strategy.
 * <p>
 * Supports multiple calculation strategies such as JavaScript and Python.
 * The implementation is loaded dynamically and initialized at startup.
 * </p>
 */
@Service
public class CalculatorFactory implements ICalculatorFactory {
    private static final Logger LOGGER = LogManager.getLogger(CalculatorFactory.class);

    private IRateCalculator rateCalculator;
    private final IRateFactory rateFactory;
    private final IRateConverter rateConverter;
    private final ICalculatorLoader calculatorLoader;

    /**
     * The calculation strategy defined in application properties,
     * used to determine which calculator implementation to load.
     */
    @Value("${app.rate-calculation-strategy}")
    private String rateCalculationStrategy;

    /**
     * The path to the rate calculator implementation, loaded at initialization.
     */
    @Value("${app.rate-calculator-path}")
    private String rateCalculatorPath;

    /**
     * Constructs the CalculatorFactory with required dependencies.
     *
     * @param rateFactory      factory to create rate instances
     * @param rateConverter    converter to handle rate conversions
     * @param calculatorLoader loader responsible for loading calculator implementations
     */
    @Autowired
    public CalculatorFactory(IRateFactory rateFactory, IRateConverter rateConverter, ICalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.rateConverter = rateConverter;
        this.calculatorLoader = calculatorLoader;
    }

    /**
     * Initializes the calculator factory by loading the appropriate calculator
     * based on the configured calculation strategy.
     *
     * @throws CalculatorException if the strategy is unsupported or initialization fails.
     */
    @PostConstruct
    public void init() throws CalculatorException {
        try {
            CalculatorEnum calculatorEnum = CalculatorEnum.valueOf(rateCalculationStrategy);

            switch (calculatorEnum) {
                case CalculatorEnum.JAVASCRIPT -> {
                    rateCalculator = getJavascriptCalculator();
                }
                case CalculatorEnum.PYTHON -> {
                    rateCalculator = getPythonCalculator();
                }
                default ->
                        throw new CalculatorLoadingException("Unsupported rate calculation strategy: " + rateCalculationStrategy);
            }
        } catch (IllegalArgumentException e) {
            throw new CalculatorLoadingException("Unsupported rate calculation strategy: " + rateCalculationStrategy, e);
        } catch (Exception e) {
            throw new CalculatorLoadingException("Something went wrong: " + rateCalculationStrategy, e);
        }
    }

    /**
     * Returns the initialized {@link IRateCalculator}.
     *
     * @return the rate calculator instance
     * @throws CalculatorException if the calculator is not initialized.
     */
    @Override
    public IRateCalculator getCalculator() throws CalculatorException {
        if (rateCalculator != null) {
            return rateCalculator;
        } else {
            throw new CalculatorException("Rate calculator does not exist!");
        }
    }

    /**
     * Creates and initializes a JavaScript-based calculator.
     *
     * @return the initialized {@link JavascriptCalculator}
     * @throws CalculatorException if initialization fails.
     */
    private JavascriptCalculator getJavascriptCalculator() throws CalculatorException {
        var c = new JavascriptCalculator(rateFactory, rateConverter, calculatorLoader);
        c.init(rateCalculatorPath);

        return c;
    }

    /**
     * Creates and initializes a Python-based calculator.
     *
     * @return the initialized {@link PythonCalculator}
     * @throws CalculatorException if initialization fails.
     */
    private PythonCalculator getPythonCalculator() throws CalculatorException {
        var c = new PythonCalculator(rateFactory, rateConverter, calculatorLoader);
        c.init(rateCalculatorPath);

        return c;
    }
}
