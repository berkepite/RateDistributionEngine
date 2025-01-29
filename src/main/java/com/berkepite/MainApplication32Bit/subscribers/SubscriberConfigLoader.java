package com.berkepite.MainApplication32Bit.subscribers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "subscriber")
public class SubscriberConfigLoader {
    @Value("${subscriber.properties-path}")
    String propertiesPath;

    public Properties readFromFile(String subscriberName) throws IOException {
        Properties properties = new Properties();
        FileInputStream input;

        try {
            input = new FileInputStream(propertiesPath + subscriberName + ".properties");
            properties.load(input);

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File not found -> " + propertiesPath + subscriberName + ".properties");
        }

        return properties;
    }
}