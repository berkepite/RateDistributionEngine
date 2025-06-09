package com.berkepite.RateDistributionEngine.exception;

import com.berkepite.RateDistributionEngine.common.cache.IRateCacheService;
import com.berkepite.RateDistributionEngine.common.calculator.IRateCalculator;
import com.berkepite.RateDistributionEngine.common.exception.cache.CacheException;
import com.berkepite.RateDistributionEngine.common.exception.calculator.CalculatorException;
import com.berkepite.RateDistributionEngine.common.exception.producer.ProducerException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.*;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriber;
import com.berkepite.RateDistributionEngine.email.EmailService;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Centralized handler for managing exceptions across the Rate Distribution Engine.
 * Handles logging of exceptions and triggers email notifications for critical errors.
 * Supports detailed logging based on active Spring profiles.
 */
@Component
public class ExceptionHandler {
    private static final Logger LOGGER = LogManager.getLogger(ExceptionHandler.class);

    private final Environment environment;
    private final EmailService emailService;
    private boolean isDebugEnabled = false;

    /**
     * Constructs the ExceptionHandler with required dependencies.
     *
     * @param environment  Spring Environment to detect active profiles.
     * @param emailService Service to send alert emails on critical exceptions.
     */
    @Autowired
    public ExceptionHandler(Environment environment, EmailService emailService) {
        this.environment = environment;
        this.emailService = emailService;
    }

    /**
     * Initializes the handler by checking if 'debug' profile is active
     * to enable detailed exception logging.
     */
    @PostConstruct
    public void init() {
        isDebugEnabled = Arrays.asList(environment.getActiveProfiles()).contains("debug");
    }

    /**
     * Handles exceptions related to calculators with contextual information.
     *
     * @param e          The CalculatorException caught.
     * @param calculator The rate calculator instance involved.
     */
    public void handle(CalculatorException e, IRateCalculator calculator) {
        LOGGER.error("({}) calculator error: {}", calculator.getStrategy() + '\\' + calculator.getPath(),
                isDebugEnabled ? e : e.getMessage());
    }

    /**
     * Handles general calculator exceptions.
     *
     * @param e The CalculatorException caught.
     */
    public void handle(CalculatorException e) {
        LOGGER.error("calculator error: {}", isDebugEnabled ? e : e.getMessage());
    }

    /**
     * Handles general subscriber exceptions.
     *
     * @param e The SubscriberException caught.
     */
    public void handle(SubscriberException e) {
        LOGGER.error("subscriber error: {}", isDebugEnabled ? e : e.getMessage());
    }

    /**
     * Handles subscriber exceptions with subscriber context,
     * and sends notification emails for critical subscriber issues.
     *
     * @param e          The SubscriberException caught.
     * @param subscriber The subscriber instance involved.
     */
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

    /**
     * Handles cache exceptions with cache service context.
     *
     * @param e            The CacheException caught.
     * @param cacheService The cache service instance involved.
     */
    public void handle(CacheException e, IRateCacheService cacheService) {
        LOGGER.error("({}) cache error: {}", cacheService.getName(),
                isDebugEnabled ? e : e.getMessage());
    }

    /**
     * Handles general cache exceptions.
     *
     * @param e The CacheException caught.
     */
    public void handle(CacheException e) {
        LOGGER.error("cache error: {}",
                isDebugEnabled ? e : e.getMessage());
    }

    /**
     * Handles exceptions occurring in Kafka producers.
     *
     * @param e The ProducerException caught.
     */
    public void handle(ProducerException e) {
        LOGGER.error("kafka producer error: {}",
                isDebugEnabled ? e : e.getMessage());
    }
}
