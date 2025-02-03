package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.rates.RateEnum;

import java.util.List;

public class SubscriberConfig {
    private String name;
    private String className;
    private String classPath;
    private String url;
    private String username;
    private String password;
    private List<RateEnum> includeRates;
    private List<RateEnum> excludeRates;

    @Override
    public String toString() {
        return "SubscriberModel [name=" + name + "," +
                " className=" + className + "," +
                " classPath=" + classPath + "," +
                " url=" + url + "," +
                " includeRates=" + includeRates + "," +
                " excludeRates=" + excludeRates + "," +
                " username=" + "*****" + "," +
                " password=" + "*****" + "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<RateEnum> getExcludeRates() {
        return excludeRates;
    }

    public void setExcludeRates(List<RateEnum> excludeRates) {
        this.excludeRates = excludeRates;
    }

    public List<RateEnum> getIncludeRates() {
        return includeRates;
    }

    public void setIncludeRates(List<RateEnum> includeRates) {
        this.includeRates = includeRates;
    }
}
