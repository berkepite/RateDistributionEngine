package com.berkepite.RateDistributionEngine.common;


import java.util.List;

public interface ISubscriberConfig {
    String getName();

    String getClassName();

    String getClassPath();

    String getUrl();

    Integer getPort();

    String getUsername();

    String getPassword();

    List<String> getIncludeRates();

    List<String> getExcludeRates();

    void setName(String name);

    void setClassName(String className);

    void setClassPath(String classPath);

    void setUrl(String url);

    void setPort(Integer port);

    void setUsername(String username);

    void setPassword(String password);

    void setIncludeRates(List<String> rates);

    void setExcludeRates(List<String> rates);

}
