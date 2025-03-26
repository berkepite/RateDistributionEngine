package com.berkepite.RateDistributionEngine.subscribers;

import com.berkepite.RateDistributionEngine.MainApplication32BitApplication;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.ISubscriber;
import com.berkepite.RateDistributionEngine.common.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.coordinator.CoordinatorConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

@Component
public class SubscriberLoader {
    private static final Logger LOGGER = LogManager.getLogger(SubscriberLoader.class);

    public ISubscriber load(CoordinatorConfig.SubscriberBindingConfig bindingConfig, ICoordinator coordinator) {
        try {
            // Path to the JAR file
            File jarFile = new File("subscribers/" + bindingConfig.getJarName());
            URL jarURL = jarFile.toURI().toURL();  // Convert the file path to a URL

            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL}, MainApplication32BitApplication.class.getClassLoader());

            // Load the class dynamically
            Class<?> loadedClass = classLoader.loadClass(bindingConfig.getClassPath());

            // Get the class for the config
            Class<?> loadedConfigClass = classLoader.loadClass(bindingConfig.getConfigClassPath());

            // Load the config file
            ISubscriberConfig subscriberConfig = SubscriberConfigLoader.load(bindingConfig.getConfigName(), loadedConfigClass);

            // Optionally, create an instance using reflection
            ISubscriber instance = (ISubscriber) loadedClass.getDeclaredConstructor(ICoordinator.class, ISubscriberConfig.class).newInstance(coordinator, subscriberConfig);

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
