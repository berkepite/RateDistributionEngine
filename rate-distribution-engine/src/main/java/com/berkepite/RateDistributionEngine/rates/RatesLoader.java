package com.berkepite.RateDistributionEngine.rates;

import com.berkepite.RateDistributionEngine.common.rates.IRatesLoader;
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

@Service
public class RatesLoader implements IRatesLoader {
    Logger LOGGER = LogManager.getLogger(RatesLoader.class);

    @Value("${app.coordinator.rates}")
    private String rates;

    private List<String> ratesList;

    public RatesLoader() {
        ratesList = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        loadRates();
    }

    @Override
    public List<String> getRatesList() {
        return new ArrayList<>(ratesList);
    }

    private void loadRates() {
        if (rates == null) {
            throw new RuntimeException("No 'rates' entry found in application config (app.coordinator.rates)!");
        }

        Path path = Paths.get(rates);

        if (Files.exists(path) && Files.isReadable(path)) {
            try {
                String line = Files.readAllLines(path).getFirst();

                if (line.matches("^([A-Z]{3}_[A-Z]{3})(,[A-Z]{3}_[A-Z]{3})*$")) {
                    ratesList = Arrays.asList(line.split(","));
                } else {
                    throw new RuntimeException("CSV file structure is invalid! Rates must be in 'XXX_YYY' format!");
                }
            } catch (Exception e) {
                LOGGER.error("Could not load rates from the csv file!", e);
            }
        } else if (rates.matches("^([A-Z]{3}_[A-Z]{3})(,[A-Z]{3}_[A-Z]{3})*$")) {
            try {
                var k = rates.split(",");
                ratesList = Arrays.asList(k);
            } catch (Exception e) {
                LOGGER.error("Could not load rates from plain input!");
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.error("Could not load rates! Please check your application config! Your csv file may not be readable!");
            throw new RuntimeException();
        }
    }
}
