package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.ConfigMapper;
import com.berkepite.MainApplication32Bit.coordinator.ICoordinator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Properties;

@Component
public class SubscriberLoader {

    private static final Logger LOGGER = LogManager.getLogger(SubscriberLoader.class);
    private final SubscriberConfigLoader subscriberConfigLoader;
    private final ConfigMapper configMapper;

    @Autowired
    public SubscriberLoader(SubscriberConfigLoader subscriberConfigLoader, ConfigMapper configMapper) {
        this.subscriberConfigLoader = subscriberConfigLoader;
        this.configMapper = configMapper;
    }

    public ISubscriber load(SubscriberBindingConfig bindingConfig, ICoordinator coordinator) {
        ISubscriber subscriber = null;
        try {
            Properties props = subscriberConfigLoader.readFromFile(bindingConfig.getConfigName());
            ISubscriberConfig config = configMapper.mapSubscriberConfig(props);
            subscriber = loadAsClass(config, coordinator);
        } catch (Exception e) {
            LOGGER.error("Failed to load subscriber config", e);
        }

        return subscriber;
    }

    private ISubscriber loadAsClass(ISubscriberConfig subscriberConfig, ICoordinator coordinator) throws ClassNotFoundException {
        ISubscriber subscriber = null;

        if (subscriberConfig == null) {
            LOGGER.error("Failed to load subscriber class because subscriber config is null!");
            return null;
        }

        try {
            String className = subscriberConfig.getClassPath() + "." + subscriberConfig.getClassName();

            Class<?> clazz = Class.forName(className);

            ISubscriber instance = (ISubscriber) MethodHandles.lookup()
                    .findConstructor(clazz, MethodType.methodType(void.class))
                    .invoke();

            instance.setConfig(subscriberConfig);
            instance.setCoordinator(coordinator);

            subscriber = instance;
        } catch (Throwable e) {
            LOGGER.error("Failed to load subscriber class {}, {}", subscriberConfig.getClassName(), e);
        }

        return subscriber;
    }
}
