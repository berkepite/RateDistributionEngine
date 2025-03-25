package com.berkepite.RateDistributionEngine.coordinator;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
        private String configClassPath;

        public String getConfigClassPath() {
            return configClassPath;
        }

        public void setConfigClassPath(String configClassPath) {
            this.configClassPath = configClassPath;
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
            return "SubscriberBindingConfig{" +
                    "name='" + name + '\'' +
                    ", enabled=" + enabled +
                    ", jarName='" + jarName + '\'' +
                    ", configName='" + configName + '\'' +
                    ", classPath='" + classPath + '\'' +
                    ", configClassPath='" + configClassPath + '\'' +
                    '}';
        }
    }
}