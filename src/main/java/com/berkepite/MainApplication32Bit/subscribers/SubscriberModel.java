package com.berkepite.MainApplication32Bit.subscribers;

public class SubscriberModel {
    private String name;
    private boolean enabled;
    private String configPath;

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

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public String toString() {
        return "Subscriber{name='" + name + "', enabled=" + enabled + ", configPath='" + configPath + "'}";
    }
}
