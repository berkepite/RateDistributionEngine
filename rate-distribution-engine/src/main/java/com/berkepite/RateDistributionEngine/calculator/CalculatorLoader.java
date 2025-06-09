package com.berkepite.RateDistributionEngine.calculator;

import com.berkepite.RateDistributionEngine.common.calculator.ICalculatorLoader;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorLoadingException;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of {@link ICalculatorLoader} responsible for loading
 * calculator files from the filesystem.
 * <p>
 * Validates the existence of the given path and returns it as a {@link Path}.
 * Throws an exception if the file does not exist or loading fails.
 * </p>
 */
@Component
public class CalculatorLoader implements ICalculatorLoader {

    /**
     * Loads the calculator file located at the specified path.
     *
     * @param path the file path to load
     * @return the {@link Path} object pointing to the calculator file
     * @throws CalculatorLoadingException if the file does not exist or cannot be loaded
     */
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
