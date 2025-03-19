package com.berkepite.RateDistributionEngine.coordinator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.coordinator")
public class CoordinatorConfig {

    private List<SubscriberBindingConfig> subscribers;
    private List<String> rates;

    public List<String> getRates() {
        return rates;
    }

    public List<SubscriberBindingConfig> getSubscribers() {
        return subscribers;
    }

    public void setRates(List<String> rates) {
        this.rates = rates;
    }

    public void setSubscribers(List<SubscriberBindingConfig> subscribers) {
        this.subscribers = subscribers;
    }

    public static class SubscriberBindingConfig {
        private String name;
        private boolean enabled;
        private String jarName;
        private String configName;
        private String classPath;
        private String className;

        public String getClassName() {
            return className;
        }

        public String getClassPath() {
            return classPath;
        }

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getConfigName() {
            return configName;
        }

        public void setClassPath(String classPath) {
            this.classPath = classPath;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public void setConfigName(String configName) {
            this.configName = configName;
        }

        public String getJarName() {
            return jarName;
        }

        public void setJarName(String jarName) {
            this.jarName = jarName;
        }

        @Override
        public String toString() {
            return "Subscriber{name='" + name + "', enabled=" + enabled + ", configName='" + configName + "'}";
        }
    }
}