package com.berkepite.MainApplication32Bit.coordinator;

import com.berkepite.MainApplication32Bit.subscribers.ISubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Coordinator implements CommandLineRunner {

    private final CoordinatorConfig coordinatorConfig;

    @Autowired
    public Coordinator(CoordinatorConfig coordinatorConfig) {
        this.coordinatorConfig = coordinatorConfig;
        System.out.println("Coordinator Initialized!");
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
