package com.berkepite.RateDistributionEngine.calculator;

import com.berkepite.RateDistributionEngine.common.calculator.ICalculatorLoader;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorLoadingException;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class CalculatorLoader implements ICalculatorLoader {
    public Path load(String path) throws CalculatorLoadingException {
        try {
            Path calculatorPath = Paths.get(path);

            if (!Files.exists(calculatorPath)) {
                throw new CalculatorLoadingException("Calculator file was not found: " + path);
            }

            return calculatorPath;

        } catch (Exception ex) {
            throw new CalculatorLoadingException("Calculator file could not be loaded!", ex);
        }
    }
}
