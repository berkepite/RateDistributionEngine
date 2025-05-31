package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.common.calculators.CalculatorEnum;
import com.berkepite.RateDistributionEngine.common.calculators.ICalculatorFactory;
import com.berkepite.RateDistributionEngine.common.calculators.ICalculatorLoader;
import com.berkepite.RateDistributionEngine.common.calculators.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.rates.IRateConverter;
import com.berkepite.RateDistributionEngine.common.rates.IRateFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculatorFactory implements ICalculatorFactory {
    private static final Logger LOGGER = LogManager.getLogger(CalculatorFactory.class);

    private final IRateFactory rateFactory;
    private final IRateConverter rateConverter;
    private final ICalculatorLoader calculatorLoader;

    @Autowired
    public CalculatorFactory(IRateFactory rateFactory, IRateConverter rateConverter, ICalculatorLoader calculatorLoader) {
        this.rateFactory = rateFactory;
        this.rateConverter = rateConverter;
        this.calculatorLoader = calculatorLoader;
    }

    @Override
    public IRateCalculator getCalculator(CalculatorEnum strategy, String path) {
        try {
            switch (strategy) {
                case CalculatorEnum.JAVASCRIPT -> {
                    return getJavascriptCalculator(path);
                }
                case CalculatorEnum.PYTHON -> {
                    return getPythonCalculator(path);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while creating calculator, terminating...\n {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    private JavascriptCalculator getJavascriptCalculator(String path) throws Exception {
        var c = new JavascriptCalculator(rateFactory, rateConverter, calculatorLoader);
        c.init(path);

        return c;
    }

    private PythonCalculator getPythonCalculator(String path) throws Exception {
        var c = new PythonCalculator(rateFactory, rateConverter, calculatorLoader);
        c.init(path);

        return c;
    }
}