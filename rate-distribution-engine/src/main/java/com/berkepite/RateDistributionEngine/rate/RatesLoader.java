package com.berkepite.RateDistributionEngine.rate;

import com.berkepite.RateDistributionEngine.common.rate.IRatesLoader;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class responsible for loading currency rates from a configuration source.
 * <p>
 * It supports loading rates either from a CSV file or a plain CSV string specified
 * in the application configuration (app.coordinator.rates).
 * </p>
 */
@Service
public class RatesLoader implements IRatesLoader {
    private static final Logger LOGGER = LogManager.getLogger(RatesLoader.class);

    /**
     * Configuration property specifying the rates input.
     * Can be a path to a CSV file or a plain CSV string.
     */
    @Value("${app.coordinator.rates}")
    private String rates;

    private List<String> ratesList;

    /**
     * Constructs a new RatesLoader with an empty rates list.
     */
    public RatesLoader() {
        ratesList = new ArrayList<>();
    }

    /**
     * Initializes the RatesLoader by loading the rates after bean construction.
     */
    @PostConstruct
    public void init() {
        loadRates();
    }

    /**
     * Returns a copy of the loaded rates list.
     *
     * @return a list of currency rates in 'XXX_YYY' format
     */
    @Override
    public List<String> getRatesList() {
        return new ArrayList<>(ratesList);
    }

    /**
     * Loads rates from the configured source.
     * <p>
     * This method attempts to read rates from a file if the configuration string
     * represents a valid readable file path. Otherwise, it treats the configuration
     * string as a plain CSV of rates.
     * </p>
     *
     * @throws RuntimeException if the configuration is invalid or rates cannot be loaded
     */
    private void loadRates() {
        if (rates == null) {
            throw new RuntimeException("No 'rates' entry found in application config (app.coordinator.rates)!");
        }

        Path path = Paths.get(rates);

        if (Files.exists(path) && Files.isReadable(path)) {
            try {
                String line = Files.readAllLines(path).get(0);

                if (line.matches("^([A-Z]{3}_[A-Z]{3})(,[A-Z]{3}_[A-Z]{3})*$")) {
                    ratesList = Arrays.asList(line.split(","));
                } else {
                    throw new RuntimeException("CSV file structure is invalid! Rates must be in 'XXX_YYY' format!");
                }
            } catch (Exception e) {
                LOGGER.error("Could not load rates from the CSV file!", e);
            }
        } else if (rates.matches("^([A-Z]{3}_[A-Z]{3})(,[A-Z]{3}_[A-Z]{3})*$")) {
            try {
                ratesList = Arrays.asList(rates.split(","));
            } catch (Exception e) {
                LOGGER.error("Could not load rates from plain input!", e);
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.error("Could not load rates! Please check your application config! Your CSV file may not be readable!");
            throw new RuntimeException();
        }
    }
}
