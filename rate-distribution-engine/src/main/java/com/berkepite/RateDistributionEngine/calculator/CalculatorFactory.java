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

@Service
public class CalculatorFactory implements ICalculatorFactory {
    private static final Logger LOGGER = LogManager.getLogger(CalculatorFactory.class);

    private IRateCalculator rateCalculator;
    private final IRateFactory rateFactory;
    private final IRateConverter rateConverter;
    private final ICalculatorLoader calculatorLoader;

    @Value("${app.rate-calculation-strategy}")
    private String rateCalculationStrategy;

    @Value("${app.rate-calculator-path}")
    private String rateCalculatorPath;

    @Autowired
    public CalculatorFactory(IRateFactory rateFactory, IRateConverter rateConverter, ICalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.rateConverter = rateConverter;
        this.calculatorLoader = calculatorLoader;
    }

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

    @Override
    public IRateCalculator getCalculator() throws CalculatorException {
        if (!(rateCalculator == null)) {
            return rateCalculator;
        } else {
            throw new CalculatorException("Rate calculator does not exist!");
        }
    }

    private JavascriptCalculator getJavascriptCalculator() throws CalculatorException {
        var c = new JavascriptCalculator(rateFactory, rateConverter, calculatorLoader);
        c.init(rateCalculatorPath);

        return c;
    }

    private PythonCalculator getPythonCalculator() throws CalculatorException {
        var c = new PythonCalculator(rateFactory, rateConverter, calculatorLoader);
        c.init(rateCalculatorPath);

        return c;
    }
}