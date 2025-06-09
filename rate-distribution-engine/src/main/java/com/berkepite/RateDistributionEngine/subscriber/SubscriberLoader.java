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

/**
 * Implementation of {@link ISubscriberLoader} responsible for loading subscriber instances dynamically
 * from JAR files based on the provided binding configuration.
 */
@Component
public class SubscriberLoader implements ISubscriberLoader {

    /**
     * Loads a subscriber instance from the specified JAR file using the provided binding configuration
     * and coordinator.
     *
     * @param bindingConfig the subscriber binding configuration containing JAR and class information.
     * @param coordinator   the coordinator instance to pass to the subscriber constructor.
     * @return a new instance of {@link ISubscriber} loaded dynamically.
     * @throws SubscriberLoadingException if any error occurs during loading or instantiation of the subscriber.
     */
    @Override
    public ISubscriber load(ISubscriberBindingConfig bindingConfig, ICoordinator coordinator) throws SubscriberLoadingException {
        try {
            String jarPath = "subscribers/" + bindingConfig.getJarName();
            File jarFile = new File(jarPath);

            if (!jarFile.exists()) {
                throw new SubscriberLoadingException("Subscriber JAR file does not exist! (" + jarPath + ")");
            }

            URL jarURL = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL}, RateDistributionEngineApplication.class.getClassLoader());

            ISubscriberConfig subscriberConfig = loadConfig(classLoader, bindingConfig);
            ISubscriber instance = loadSubscriber(classLoader, bindingConfig, coordinator, subscriberConfig);

            classLoader.close();

            return instance;
        } catch (Exception e) {
            throw new SubscriberLoadingException("Something went wrong while loading Subscriber! (" + e.getMessage() + ")", e);
        }
    }

    /**
     * Loads the subscriber class, creates an instance using a constructor that takes a coordinator and subscriber config.
     *
     * @param classLoader      the class loader to load subscriber classes.
     * @param bindingConfig    the subscriber binding configuration.
     * @param coordinator      the coordinator instance.
     * @param subscriberConfig the subscriber configuration instance.
     * @return a new subscriber instance.
     * @throws SubscriberLoadingException if loading or instantiation fails.
     */
    private ISubscriber loadSubscriber(URLClassLoader classLoader, ISubscriberBindingConfig bindingConfig,
                                       ICoordinator coordinator, ISubscriberConfig subscriberConfig) throws SubscriberLoadingException {
        try {
            Class<?> loadedClass = classLoader.loadClass(bindingConfig.getClassPath());
            return (ISubscriber) loadedClass.getDeclaredConstructor(ICoordinator.class, ISubscriberConfig.class)
                    .newInstance(coordinator, subscriberConfig);

        } catch (ClassNotFoundException e) {
            throw new SubscriberClassNotFoundException("Subscriber Class does not exist: " + e, e.getCause());
        } catch (NoSuchMethodException | IllegalArgumentException e) {
            throw new SubscriberProperConstructorNotFoundException("Subscriber class does not have a proper constructor: " + e, e.getCause());
        } catch (Exception e) {
            throw new SubscriberLoadingException("Failed to load subscriber class: " + e, e.getCause());
        }
    }

    /**
     * Loads the subscriber configuration from the class loader and binding config.
     *
     * @param classLoader   the class loader to load config classes.
     * @param bindingConfig the subscriber binding configuration.
     * @return an instance of {@link ISubscriberConfig} loaded from configuration file.
     * @throws SubscriberLoadingException if loading config fails.
     */
    private ISubscriberConfig loadConfig(URLClassLoader classLoader, ISubscriberBindingConfig bindingConfig) throws SubscriberLoadingException {
        try {
            Class<?> loadedConfigClass = classLoader.loadClass(bindingConfig.getConfigClassPath());
            return SubscriberConfigLoader.load(bindingConfig.getConfigName(), loadedConfigClass);

        } catch (ClassNotFoundException e) {
            throw new SubscriberClassNotFoundException("Subscriber config class does not exist: " + e, e.getCause());
        } catch (FileNotFoundException e) {
            throw new SubscriberLoadingException("Subscriber config file can't be found!: " + e, e.getCause());
        } catch (Exception e) {
            throw new SubscriberLoadingException("Failed to load subscriber config: " + e, e.getCause());
        }
    }
}
