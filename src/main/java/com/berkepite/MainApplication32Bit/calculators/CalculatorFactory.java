package com.berkepite.MainApplication32Bit.calculators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CalculatorFactory {
    public final Logger LOGGER = LogManager.getLogger(CalculatorFactory.class);

    public IRateCalculator getCalculator(String strategy, String sourcePath) {
        try {

            switch (strategy) {
                case "Javascript" -> {
                    return getJavascriptCalculator(sourcePath);
                }
                case "Python" -> {
                    return null; // WIP
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while creating calculator, terminating...\n {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return null; // WIP
    }

    private JavascriptCalculator getJavascriptCalculator(String sourcePath) throws Exception {

        return new JavascriptCalculator(sourcePath);
    }
}