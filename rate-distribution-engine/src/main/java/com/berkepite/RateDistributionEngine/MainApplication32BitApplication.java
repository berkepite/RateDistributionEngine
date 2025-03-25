package com.berkepite.RateDistributionEngine;

import com.berkepite.RateDistributionEngine.coordinator.CoordinatorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CoordinatorConfig.class)
public class MainApplication32BitApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication32BitApplication.class, args);
    }

}
