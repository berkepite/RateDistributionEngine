package com.berkepite.RateDistributionEngine.config;

import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.raw-rate-topic}")
    private String rawRateTopic;

    @Value("${app.kafka.calc-rate-topic}")
    private String calcRateTopic;

    @Bean
    public KafkaTemplate<String, RawRate> kafkaRawRateTemplate() {
        KafkaTemplate<String, RawRate> template = new KafkaTemplate<>(rawRateProducerFactory());
        template.setDefaultTopic(rawRateTopic);
        return template;
    }

    @Bean
    public KafkaTemplate<String, CalculatedRate> kafkaCalcRateTemplate() {
        KafkaTemplate<String, CalculatedRate> template = new KafkaTemplate<>(calcRateProducerFactory());
        template.setDefaultTopic(calcRateTopic);
        return template;
    }

    @Bean
    public ProducerFactory<String, RawRate> rawRateProducerFactory() {
        return new DefaultKafkaProducerFactory<>(rawRateProducerConfig());
    }

    @Bean
    public ProducerFactory<String, CalculatedRate> calcRateProducerFactory() {
        return new DefaultKafkaProducerFactory<>(calcRateProducerConfig());
    }

    private Map<String, Object> rawRateProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configProps;
    }

    private Map<String, Object> calcRateProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configProps;
    }
}
