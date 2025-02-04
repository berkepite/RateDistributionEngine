package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.ConfigMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Properties;

@SpringBootTest
public class ConfigMapperTest {

    @Autowired
    private ConfigMapper mapper;

    @Value("${coordinator.config-name}")
    private String configName;

    @Test
    public void testmapSubscriberBindingConfigs_OK() {

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource(configName));
        Properties props = yaml.getObject();

        Assertions.assertNotNull(props);

        List<SubscriberBindingConfig> list = mapper.mapSubscriberBindingConfigs(props);

        Assertions.assertNotNull(list);
    }

    @Test
    public void testmapSubscriberConfig_OK() {

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource(configName));
        Properties props = yaml.getObject();

        Assertions.assertNotNull(props);

        SubscriberConfig model = mapper.mapSubscriberConfig(props);

        Assertions.assertNotNull(model);
    }
}
