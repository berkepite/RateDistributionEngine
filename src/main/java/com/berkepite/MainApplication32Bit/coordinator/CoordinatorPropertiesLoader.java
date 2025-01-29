package com.berkepite.MainApplication32Bit.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class CoordinatorPropertiesLoader {

    private final CoordinatorProperties coordinatorProperties;

    public CoordinatorPropertiesLoader(@Value("${coordinator.config-path}") String configPath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(configPath);

            // Read JSON and map to CurrencyConfig
            this.coordinatorProperties = objectMapper.readValue(file, CoordinatorProperties.class);
        } catch (IOException e) {
            throw new RuntimeException("Error loading initial data from file: " + configPath, e);
        }
    }

    public CoordinatorProperties getCoordinatorProperties() {
        return coordinatorProperties;
    }
}
