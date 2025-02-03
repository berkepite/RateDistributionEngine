package com.berkepite.MainApplication32Bit.subscribers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class SubscriberConfigLoaderTest {

    @Autowired
    SubscriberConfigLoader subscriberConfigLoader;

    @Test
    public void testLoadSubscriberConfig_validFile() {
        Properties properties;
        try {
            properties = subscriberConfigLoader.readFromFile("subscriber");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertNotNull(properties, "Properties should not be null");
        assertEquals("subscriber", properties.getProperty("name"));
    }

    @Test
    public void testLoadSubscriberConfig_fileNotFound() {
        Exception exception = null;
        try {
            Properties properties = subscriberConfigLoader.readFromFile("invalidName");

        } catch (Exception e) {
            exception = e;
        }

        // Then an exception should be thrown
        assertNotNull(exception, "Exception should be thrown when file is not found");
    }
}
