package com.berkepite.RateDistributionEngine.subscribers;

import com.berkepite.RateDistributionEngine.MainApplication32BitApplication;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.ISubscriber;
import com.berkepite.RateDistributionEngine.common.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberClassNotFoundException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberLoadingException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberProperConstructorNotFoundException;
import com.berkepite.RateDistributionEngine.coordinator.CoordinatorConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;

@Component
public class SubscriberLoader {
    private static final Logger LOGGER = LogManager.getLogger(SubscriberLoader.class);

    public ISubscriber load(CoordinatorConfig.SubscriberBindingConfig bindingConfig, ICoordinator coordinator) {
        try {
            // Path to the JAR file
            String jarPath = "subscribers/" + bindingConfig.getJarName();
            File jarFile = new File(jarPath);

            if (!jarFile.exists()) {
                throw new SubscriberLoadingException("Subscriber JAR file does not exist! (%s) ".formatted(jarPath));
            }

            URL jarURL = jarFile.toURI().toURL();  // Convert the file path to a URL
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL}, MainApplication32BitApplication.class.getClassLoader());

            ISubscriberConfig subscriberConfig = loadConfig(classLoader, bindingConfig);
            ISubscriber instance = loadSubscriber(classLoader, bindingConfig, coordinator, subscriberConfig);

            // Close the class loader
            classLoader.close();

            return instance;
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }

    private ISubscriber loadSubscriber(URLClassLoader classLoader, CoordinatorConfig.SubscriberBindingConfig bindingConfig, ICoordinator coordinator, ISubscriberConfig subscriberConfig) throws SubscriberLoadingException {
        try {
            Class<?> loadedClass = classLoader.loadClass(bindingConfig.getClassPath());
            return (ISubscriber) loadedClass.getDeclaredConstructor(ICoordinator.class, ISubscriberConfig.class).newInstance(coordinator, subscriberConfig);

        } catch (ClassNotFoundException e) {
            throw new SubscriberClassNotFoundException("Subscriber Class does not exist: %s".formatted(e), e.getCause());
        } catch (NoSuchMethodException | IllegalArgumentException e) {
            throw new SubscriberProperConstructorNotFoundException("Subscriber class does not have a proper constructor: %s".formatted(e), e.getCause());
        } catch (Exception e) {
            throw new SubscriberLoadingException("Failed to load subscriber class: %s".formatted(e), e.getCause());
        }
    }

    private ISubscriberConfig loadConfig(URLClassLoader classLoader, CoordinatorConfig.SubscriberBindingConfig bindingConfig) throws SubscriberLoadingException {
        try {
            Class<?> loadedConfigClass = classLoader.loadClass(bindingConfig.getConfigClassPath());
            return SubscriberConfigLoader.load(bindingConfig.getConfigName(), loadedConfigClass);
        } catch (ClassNotFoundException e) {
            throw new SubscriberClassNotFoundException("Subscriber config class does not exist: %s".formatted(e), e.getCause());
        } catch (FileNotFoundException e) {
            throw new SubscriberLoadingException("Subscriber config class can't be found!: %s".formatted(e), e.getCause());
        } catch (Exception e) {
            throw new SubscriberLoadingException("Failed to load subscriber class: %s".formatted(e), e.getCause());
        }
    }
}
