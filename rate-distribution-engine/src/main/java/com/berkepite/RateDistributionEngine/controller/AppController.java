package com.berkepite.RateDistributionEngine.controller;

import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AppController {
    private static final Logger LOGGER = LogManager.getLogger(AppController.class);
    private final ICoordinator coordinator;

    @Autowired
    public AppController(ICoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/subscribers")
    public List<ISubscriber> subscribers() {
        return coordinator.getSubscribers();
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String subscriber, @RequestBody List<String> rates) {
        LOGGER.warn("Received subscription request: {} - {}", subscriber, rates.toString());
        return coordinator.subscribe(subscriber, rates);
    }

    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String subscriber, @RequestBody List<String> rates) {
        LOGGER.warn("Received unsubscription request: {} - {}", subscriber, rates.toString());
        return coordinator.unSubscribe(subscriber, rates);
    }

    @PostMapping("/connect")
    public String connect(@RequestParam String subscriber) {
        LOGGER.warn("Received connect request: {}", subscriber);
        return coordinator.connect(subscriber);
    }

    @PostMapping("/disconnect")
    public String disconnect(@RequestParam String subscriber) {
        LOGGER.warn("Received disconnect request: {}", subscriber);
        return coordinator.disconnect(subscriber);
    }
}
