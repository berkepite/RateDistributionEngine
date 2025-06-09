package com.berkepite.RateDistributionEngine.controller;

import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller that provides endpoints to manage subscriber connections and subscriptions.
 * <p>
 * Allows clients to ping the server, list subscribers, and send requests to connect,
 * disconnect, subscribe, or unsubscribe subscribers to rates.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class AppController {
    private static final Logger LOGGER = LogManager.getLogger(AppController.class);
    private final ICoordinator coordinator;

    /**
     * Constructs the AppController with the given coordinator.
     *
     * @param coordinator the coordinator service managing subscribers and rates.
     */
    @Autowired
    public AppController(ICoordinator coordinator) {
        this.coordinator = coordinator;
    }

    /**
     * Health check endpoint that returns a simple "pong" response.
     *
     * @return the string "pong".
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    /**
     * Retrieves the list of all subscriber names.
     *
     * @return a list of subscriber names.
     */
    @GetMapping("/subscribers")
    public List<String> subscribers() {
        return coordinator.getSubscribers()
                .stream()
                .map(s -> s.getConfig().getName())
                .toList();
    }

    /**
     * Subscribes a subscriber to a list of rates.
     * <p>
     * The subscriber's name is provided as a query parameter, and the list of rates
     * is provided in the request body.
     * </p>
     *
     * @param subscriber the name of the subscriber to subscribe.
     * @param rates      the list of rates to subscribe to.
     * @return a result message indicating success or failure.
     */
    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String subscriber, @RequestBody List<String> rates) {
        LOGGER.warn("Received subscription request: {} - {}", subscriber, rates.toString());
        return coordinator.subscribe(subscriber, rates);
    }

    /**
     * Unsubscribes a subscriber from a list of rates.
     * <p>
     * The subscriber's name is provided as a query parameter, and the list of rates
     * is provided in the request body.
     * </p>
     *
     * @param subscriber the name of the subscriber to unsubscribe.
     * @param rates      the list of rates to unsubscribe from.
     * @return a result message indicating success or failure.
     */
    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String subscriber, @RequestBody List<String> rates) {
        LOGGER.warn("Received unsubscription request: {} - {}", subscriber, rates.toString());
        return coordinator.unSubscribe(subscriber, rates);
    }

    /**
     * Connects the specified subscriber.
     *
     * @param subscriber the name of the subscriber to connect.
     * @return a result message indicating success or failure.
     */
    @GetMapping("/connect")
    public String connect(@RequestParam String subscriber) {
        LOGGER.warn("Received connect request: {}", subscriber);
        return coordinator.connect(subscriber);
    }

    /**
     * Disconnects the specified subscriber.
     *
     * @param subscriber the name of the subscriber to disconnect.
     * @return a result message indicating success or failure.
     */
    @GetMapping("/disconnect")
    public String disconnect(@RequestParam String subscriber) {
        LOGGER.warn("Received disconnect request: {}", subscriber);
        return coordinator.disconnect(subscriber);
    }
}
