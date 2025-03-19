package com.berkepite.RateDistributionEngine.calculators;

import com.berkepite.RateDistributionEngine.rates.RateFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class CalculatorFactory {
    private static final Logger LOGGER = LogManager.getLogger(CalculatorFactory.class);
    private final RateFactory rateFactory;

    public CalculatorFactory(RateFactory rateFactory) {
        this.rateFactory = rateFactory;
    }

    public IRateCalculator getCalculator(CalculatorEnum strategy) {
        try {

            switch (strategy) {
                case CalculatorEnum.JAVASCRIPT -> {
                    return getJavascriptCalculator();
                }
                case CalculatorEnum.PYTHON -> {
                    return getPythonCalculator();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while creating calculator, terminating...\n {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    private JavascriptCalculator getJavascriptCalculator() throws Exception {

        return new JavascriptCalculator(rateFactory);
    }

    private PythonCalculator getPythonCalculator() throws Exception {

        return new PythonCalculator(rateFactory);
    }
}