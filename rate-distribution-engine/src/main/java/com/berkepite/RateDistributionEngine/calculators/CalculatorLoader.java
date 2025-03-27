package com.berkepite.RateDistributionEngine.calculators;

import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class CalculatorLoader {
    public Reader load(String path) {
        File calculatorFile = new File(path);

        if (!calculatorFile.exists()) {
            throw new RuntimeException("Calculator file was not found: " + path);
        }

        try {
            InputStream stream = new FileInputStream(calculatorFile);
            return new InputStreamReader(stream);
        } catch (IOException ex) {
            throw new RuntimeException("Calculator file could not be converted to a stream: " + path);
        }
    }
}
