package com.berkepite.MainApplication32Bit;

import com.berkepite.MainApplication32Bit.status.ConnectionStatus;
import com.berkepite.MainApplication32Bit.status.RateStatus;
import com.berkepite.MainApplication32Bit.subscribers.ISubscriber;
import com.berkepite.MainApplication32Bit.subscribers.ISubscriberConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LogManager.getLogger(LoggingAspect.class);

    @Before("@annotation(com.berkepite.MainApplication32Bit.coordinator.CoordinatorEventStatus)")
    public void logBeforeCoordinatorEventStatus(final JoinPoint joinPoint) {
        List<Object> args = Arrays.stream(joinPoint.getArgs()).toList();

        ISubscriber subscriber = (ISubscriber) args.getFirst();
        Object status = args.getLast();

        ISubscriberConfig config = subscriber.getConfig();

        if (status instanceof ConnectionStatus connectionStatus) {
            if (connectionStatus.getException() != null) {
                LOGGER.warn("{} has encountered an error during connection {}", config.getName(), connectionStatus.toString());
            }

        } else if (status instanceof RateStatus rateStatus) {
            if (rateStatus.getException() != null) {
                LOGGER.warn("{} has encountered a problem with data {}", config.getName(), rateStatus.toString());
            }
        }
    }
}
