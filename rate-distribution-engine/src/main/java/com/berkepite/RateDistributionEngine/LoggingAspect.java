package com.berkepite.RateDistributionEngine;

import com.berkepite.RateDistributionEngine.common.status.ConnectionStatus;
import com.berkepite.RateDistributionEngine.common.status.RateStatus;
import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriber;
import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriberConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LogManager.getLogger(LoggingAspect.class);

    @Before("@annotation(com.berkepite.RateDistributionEngine.coordinator.CoordinatorEventStatus)")
    public void logBeforeCoordinatorEventStatus(final JoinPoint joinPoint) {
        List<Object> args = Arrays.stream(joinPoint.getArgs()).toList();

        ISubscriber subscriber = (ISubscriber) args.getFirst();
        Object status = args.getLast();

        ISubscriberConfig config = subscriber.getConfig();

        if (status instanceof ConnectionStatus connectionStatus) {
            LOGGER.error("{} has encountered an error during connection {}", config.getName(), connectionStatus.toString());

        } else if (status instanceof RateStatus rateStatus) {
            LOGGER.error("{} has encountered a problem with rate {}", config.getName(), rateStatus.toString());
        }
    }

    @Around("execution(* com.berkepite.RateDistributionEngine.*(..))")
    // Pointcut: Applies to all methods in the package
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis(); // Start time
        Object result = joinPoint.proceed(); // Execute the method
        long duration = System.currentTimeMillis() - start; // Calculate duration

        Signature signature = joinPoint.getSignature();

        if (duration >= 5_000) {
            LOGGER.warn("'{}' took {} seconds to execute!", signature, duration / 1000);
        }

        return result; // Return the original method's result
    }
}
