package com.berkepite.RateDistributionEngine.common.coordinator;

public interface ISubscriberBindingConfig {
    String getConfigClassPath();

    void setConfigClassPath(String configClassPath);

    String getClassPath();

    String getName();

    boolean isEnabled();

    String getConfigName();

    void setClassPath(String classPath);

    void setName(String name);

    void setEnabled(boolean enabled);

    void setConfigName(String configName);

    String getJarName();

    void setJarName(String jarName);
}
