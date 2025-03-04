package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.rates.RawRateEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "bloombergrest")
@PropertySource(value = "classpath:subscriber_configurations/bloomberg_rest-${spring.profiles.active}.properties")
public class BloombergRestConfig implements ISubscriberConfig {
    private String name;
    private String className;
    private String classPath;
    private String url;
    private Integer port;
    private String username;
    private String password;
    private Integer requestInterval;
    private Integer requestRetryLimit;
    private List<RawRateEnum> includeRates;
    private List<RawRateEnum> excludeRates;

    public void setRequestInterval(Integer requestInterval) {
        this.requestInterval = requestInterval;
    }

    public void setRequestRetryLimit(Integer requestRetryLimit) {
        this.requestRetryLimit = requestRetryLimit;
    }

    public Integer getRequestInterval() {
        return requestInterval;
    }

    public Integer getRequestRetryLimit() {
        return requestRetryLimit;
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
    public List<RawRateEnum> getIncludeRates() {
        if (null == includeRates) {
            return List.of();
        }
        return includeRates;
    }

    @Override
    public List<RawRateEnum> getExcludeRates() {
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
    public void setIncludeRates(List<RawRateEnum> includeRates) {
        this.includeRates = includeRates;
    }

    @Override
    public void setExcludeRates(List<RawRateEnum> excludeRates) {
        this.excludeRates = excludeRates;
    }


}
