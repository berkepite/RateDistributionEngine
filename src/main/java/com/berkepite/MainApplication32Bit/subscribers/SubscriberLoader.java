package com.berkepite.MainApplication32Bit.subscribers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SubscriberLoader is responsible for dynamically loading subscriber classes at runtime.
 * It registers the subscriber class as a Spring bean and returns an instance of the subscriber.
 */
@Component
public class SubscriberLoader {
    private final ApplicationContext applicationContext;
    private static final Logger LOGGER = LogManager.getLogger(SubscriberLoader.class);

    /**
     * Constructor for initializing the SubscriberLoader with the application context.
     *
     * @param applicationContext the Spring application context
     */
    public SubscriberLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Loads a subscriber class based on the provided binding configuration.
     * The class is dynamically loaded and registered as a Spring bean.
     *
     * @param bindingConfig the configuration object that contains the class path and class name
     * @return the loaded subscriber instance, or null if the loading fails
     */
    public ISubscriber load(SubscriberBindingConfig bindingConfig) {
        ISubscriber subscriber = null;

        try {
            // Construct the full class name from the provided path and class name
            String fullClassName = bindingConfig.getClassPath() + "." + bindingConfig.getClassName();
            Class<?> clazz = Class.forName(fullClassName);

            // Cast application context to ConfigurableApplicationContext to access bean factory
            ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();

            // Create a new bean definition for the subscriber class
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(clazz);
            beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

            // Register the subscriber class as a Spring bean
            beanFactory.registerBeanDefinition(bindingConfig.getClassName(), beanDefinition);

            // Retrieve the bean (subscriber) from the application context
            subscriber = (ISubscriber) context.getBean(bindingConfig.getClassName());
        } catch (Throwable e) {
            // Log any errors that occur during the loading process
            LOGGER.error("Failed to load subscriber class {}, {}", bindingConfig.getClassPath(), e);
        }

        return subscriber;
    }
}
