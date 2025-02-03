package com.berkepite.MainApplication32Bit.subscribers;

public class SubscriberConfig {
    private String name;
    private String className;
    private String classPath;
    private String url;
    private String port;
    private String username;
    private String password;

    @Override
    public String toString() {
        return "SubscriberModel [name=" + name + ", className=" + className + ", classPath=" + classPath + ", url=" + url + ", port=" + port + ", username=" + "*****" + ", password=" + "*****" + "]";
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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
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
}
