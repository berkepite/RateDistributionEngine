package com.berkepite.MainApplication32Bit.subscribers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Properties;

@SpringBootTest
@ActiveProfiles("test")
public class SubscriberMapperTest {

    @Autowired
    SubscriberMapper mapper;

    @Test
    public void testMapper_OK() {

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("coordinator-config-test.yml"));
        Properties props = yaml.getObject();

        Assertions.assertNotNull(props);

        List<SubscriberModel> list = mapper.map(props);

        Assertions.assertNotNull(list);
    }
}
