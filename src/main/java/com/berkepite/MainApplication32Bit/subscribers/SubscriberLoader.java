package com.berkepite.MainApplication32Bit.subscribers;

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
    private final SubscriberMapper subscriberMapper;

    @Autowired
    public SubscriberLoader(SubscriberConfigLoader subscriberConfigLoader, SubscriberMapper subscriberMapper) {
        this.subscriberConfigLoader = subscriberConfigLoader;
        this.subscriberMapper = subscriberMapper;
    }

    public ISubscriber load(SubscriberBindingConfig bindingConfig) {
        try {
            Properties props = subscriberConfigLoader.readFromFile(bindingConfig.getConfigName());
            SubscriberConfig model = subscriberMapper.mapSubscriberConfig(props);
            return loadAsClass(model);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private ISubscriber loadAsClass(SubscriberConfig subscriberConfig) {
        try {
            String className = subscriberConfig.getClassPath() + "." + subscriberConfig.getClassName();

            Class<?> clazz = Class.forName(className);

            ISubscriber instance = (ISubscriber) MethodHandles.lookup()
                    .findConstructor(clazz, MethodType.methodType(void.class))
                    .invoke();

            instance.setConfig(subscriberConfig);

            return instance;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
