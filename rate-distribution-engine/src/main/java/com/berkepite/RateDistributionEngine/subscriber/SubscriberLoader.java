package com.berkepite.RateDistributionEngine.subscriber;

import com.berkepite.RateDistributionEngine.RateDistributionEngineApplication;
import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import com.berkepite.RateDistributionEngine.common.coordinator.ISubscriberBindingConfig;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriber;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberClassNotFoundException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberLoadingException;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberProperConstructorNotFoundException;
import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriberLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;

@Component
public class SubscriberLoader implements ISubscriberLoader {

    @Override
    public ISubscriber load(ISubscriberBindingConfig bindingConfig, ICoordinator coordinator) throws SubscriberLoadingException {
        try {
            // Path to the JAR file
            String jarPath = "subscribers/" + bindingConfig.getJarName();
            File jarFile = new File(jarPath);

            if (!jarFile.exists()) {
                throw new SubscriberLoadingException("Subscriber JAR file does not exist! (%s) ".formatted(jarPath));
            }

            URL jarURL = jarFile.toURI().toURL();  // Convert the file path to a URL
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL}, RateDistributionEngineApplication.class.getClassLoader());

            ISubscriberConfig subscriberConfig = loadConfig(classLoader, bindingConfig);
            ISubscriber instance = loadSubscriber(classLoader, bindingConfig, coordinator, subscriberConfig);

            // Close the class loader
            classLoader.close();

            return instance;
        } catch (Exception e) {
            throw new SubscriberLoadingException("Something went wrong while loading Subscriber! (%s) ".formatted(e.getMessage()), e);
        }
    }

    private ISubscriber loadSubscriber(URLClassLoader classLoader, ISubscriberBindingConfig bindingConfig, ICoordinator coordinator, ISubscriberConfig subscriberConfig) throws SubscriberLoadingException {
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

    private ISubscriberConfig loadConfig(URLClassLoader classLoader, ISubscriberBindingConfig bindingConfig) throws SubscriberLoadingException {
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
