package com.berkepite.RateDistributionEngine.RestSubscriber;

import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriberConfig;

import java.util.List;

public class RestConfig implements ISubscriberConfig {
    private String name;
    private String className;
    private String classPath;
    private String url;
    private Integer port;
    private String username;
    private String password;
    private Integer requestInterval;
    private Integer requestRetryLimit;
    private Integer healthRequestRetryLimit;
    private List<String> includeRates;
    private List<String> excludeRates;

    public void setRequestInterval(Integer requestInterval) {
        this.requestInterval = requestInterval;
    }

    public void setRequestRetryLimit(Integer requestRetryLimit) {
        this.requestRetryLimit = requestRetryLimit;
    }

    public void setHealthRequestRetryLimit(Integer healthRequestRetryLimit) {
        this.healthRequestRetryLimit = healthRequestRetryLimit;
    }

    public Integer getRequestInterval() {
        return requestInterval;
    }

    public Integer getRequestRetryLimit() {
        return requestRetryLimit;
    }

    public Integer getHealthRequestRetryLimit() {
        return healthRequestRetryLimit;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getClassPath() {
        return classPath;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public List<String> getIncludeRates() {
        if (null == includeRates) {
            return List.of();
        }
        return includeRates;
    }

    @Override
    public List<String> getExcludeRates() {
        if (null == excludeRates) {
            return List.of();
        }
        return excludeRates;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setIncludeRates(List<String> includeRates) {
        this.includeRates = includeRates;
    }

    @Override
    public void setExcludeRates(List<String> excludeRates) {
        this.excludeRates = excludeRates;
    }


}
