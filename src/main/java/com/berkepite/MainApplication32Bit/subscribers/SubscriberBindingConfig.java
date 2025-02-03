package com.berkepite.MainApplication32Bit.subscribers;

public class SubscriberBindingConfig {
    private String name;
    private boolean enabled;
    private String configName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @Override
    public String toString() {
        return "Subscriber{name='" + name + "', enabled=" + enabled + ", configName='" + configName + "'}";
    }
}
