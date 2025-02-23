package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.rates.RawRateEnum;

import java.util.List;

public interface ISubscriberConfig {
    String getName();

    String getClassName();

    String getClassPath();

    String getUrl();

    Integer getPort();

    String getUsername();

    String getPassword();

    List<RawRateEnum> getIncludeRates();

    List<RawRateEnum> getExcludeRates();

    void setName(String name);

    void setClassName(String className);

    void setClassPath(String classPath);

    void setUrl(String url);

    void setPort(Integer port);

    void setUsername(String username);

    void setPassword(String password);

    void setIncludeRates(List<RawRateEnum> rates);

    void setExcludeRates(List<RawRateEnum> rates);

}
