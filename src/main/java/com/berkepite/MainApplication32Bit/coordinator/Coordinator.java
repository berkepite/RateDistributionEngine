package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class Coordinator implements CommandLineRunner {

    private final Properties properties;

    @Autowired
    public Coordinator(@Qualifier("subscribersProperties") Properties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        System.out.println("Coordinator Initialized! " + properties.getProperty("coordinator.subscribers[0].enabled"));
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
