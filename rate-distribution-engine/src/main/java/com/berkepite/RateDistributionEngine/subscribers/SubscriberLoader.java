package com.berkepite.RateDistributionEngine.subscribers;

import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.ISubscriber;
import com.berkepite.RateDistributionEngine.coordinator.CoordinatorConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

@Component
public class SubscriberLoader {
    private final ThreadPoolTaskExecutor executorService;
    private static final Logger LOGGER = LogManager.getLogger(SubscriberLoader.class);

    public SubscriberLoader(@Qualifier("subscriberExecutor") ThreadPoolTaskExecutor executorService) {
        this.executorService = executorService;
    }

    public ISubscriber load(CoordinatorConfig.SubscriberBindingConfig bindingConfig, ICoordinator coordinator) {
        try {
            // Path to the JAR file
            File jarFile = new File("subscribers/" + bindingConfig.getJarName());
            URL jarURL = jarFile.toURI().toURL();  // Convert the file path to a URL

            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL}, Thread.currentThread().getContextClassLoader());

            // Load the class dynamically
            Class<?> loadedClass = classLoader.loadClass(bindingConfig.getClassPath() + '.' + bindingConfig.getClassName());

            // Optionally, create an instance using reflection
            ISubscriber instance = (ISubscriber) loadedClass.getDeclaredConstructor(ICoordinator.class, ThreadPoolTaskExecutor.class).newInstance(coordinator, executorService);

            // Close the class loader (not strictly necessary in this case)
            classLoader.close();

            return instance;

        } catch (Exception e) {
            // Log any errors that occur during the loading process
            LOGGER.error("Failed to load subscriber class {}, {}", bindingConfig.getClassPath(), e);
            return null;
        }
    }
}
