package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.rates.RateEnum;

import java.util.List;

public interface ISubscriberConfig {
    String getName();

    String getClassName();

    String getClassPath();

    String getUrl();

    Integer getPort();

    String getUsername();

    String getPassword();

    List<RateEnum> getIncludeRates();

    List<RateEnum> getExcludeRates();

    void setName(String name);

    void setClassName(String className);

    void setClassPath(String classPath);

    void setUrl(String url);

    void setPort(Integer port);

    void setUsername(String username);

    void setPassword(String password);

    void setIncludeRates(List<RateEnum> rates);

    void setExcludeRates(List<RateEnum> rates);

}
