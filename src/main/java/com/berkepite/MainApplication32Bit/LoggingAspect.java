package com.berkepite.MainApplication32Bit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LogManager.getLogger(LoggingAspect.class);

    @Before("@annotation(com.berkepite.MainApplication32Bit.coordinator.CoordinatorEventStatus)")
    public void logBeforeCoordinatorEvent(final JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        LOGGER.info("Method called: {} | Parameters: {}", methodName, Arrays.toString(args));
    }
}
