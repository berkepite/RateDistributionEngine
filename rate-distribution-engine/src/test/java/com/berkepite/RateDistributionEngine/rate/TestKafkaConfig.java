package com.berkepite.RateDistributionEngine.rate;

import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestKafkaConfig {

    @Primary
    @Bean
    public KafkaTemplate<String, RawRate> rawRateKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    @Primary
    @Bean
    public KafkaTemplate<String, CalculatedRate> calculatedRateKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    @Primary
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
}
