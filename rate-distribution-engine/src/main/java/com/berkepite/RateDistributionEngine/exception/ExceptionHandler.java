package com.berkepite.RateDistributionEngine.exception;

import com.berkepite.RateDistributionEngine.common.calculators.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.*;
import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriber;
import com.berkepite.RateDistributionEngine.email.EmailService;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ExceptionHandler {
    private static final Logger LOGGER = LogManager.getLogger(ExceptionHandler.class);

    private final Environment environment;
    private final EmailService emailService;
    private boolean isDebugEnabled = false;

    @Autowired
    public ExceptionHandler(Environment environment, EmailService emailService) {
        this.environment = environment;
        this.emailService = emailService;
    }

    @PostConstruct
    public void init() {
        isDebugEnabled = Arrays.asList(environment.getActiveProfiles()).contains("debug");
    }

    public void handle(CalculatorException e, IRateCalculator calculator) {
        LOGGER.error("({}) calculator error: {}", calculator.getStrategy() + '\\' + calculator.getPath(), isDebugEnabled ? e : e.getMessage());
    }

    public void handle(SubscriberException e, ISubscriber subscriber) {
        LOGGER.error("({}) subscriber error: {}", subscriber.getConfig().getName(),
                isDebugEnabled ? e : e.getMessage());

        if (e instanceof SubscriberBadCredentialsException) {
            emailService.sendEmail(
                    "FATAL - RATE DISTRIBUTION ENGINE",
                    "Subscriber (%s) could not be authenticated for the platform (%s)!"
                            .formatted(subscriber.getConfig().getName(),
                                    subscriber.getConfig().getUrl() + subscriber.getConfig().getPort()),
                    "fatal");

        } else if (e instanceof SubscriberConnectionLostException) {
            emailService.sendEmail(
                    "WARNING - RATE DISTRIBUTION ENGINE",
                    "Subscriber (%s) lost connection to the platform (%s)!"
                            .formatted(subscriber.getConfig().getName(),
                                    subscriber.getConfig().getUrl() + subscriber.getConfig().getPort()),
                    "warn");
        }
    }
}
