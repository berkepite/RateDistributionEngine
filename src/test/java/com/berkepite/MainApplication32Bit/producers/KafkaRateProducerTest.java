package com.berkepite.MainApplication32Bit.producers;

import com.berkepite.MainApplication32Bit.rates.RateFactory;
import com.berkepite.MainApplication32Bit.rates.RawRate;
import com.berkepite.MainApplication32Bit.rates.CalculatedRate;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "app.kafka.raw-rate-topic=raw_rates",
        "app.kafka.calc-rate-topic=calc_rates"
})
@EmbeddedKafka(partitions = 1, topics = {"raw_rates", "calc_rates"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@ActiveProfiles("test")
public class KafkaRateProducerTest {

    @Value("${app.kafka.raw-rate-topic}")
    private String rawRateTopic;

    @Value("${app.kafka.calc-rate-topic}")
    private String calcRateTopic;

    @Autowired
    private KafkaTemplate<String, RawRate> kafkaRawRateTemplate;

    @Autowired
    private KafkaTemplate<String, CalculatedRate> kafkaCalcRateTemplate;

    @Autowired
    private RateFactory rateFactory;

    private Consumer<String, RawRate> rawRateConsumer;
    private Consumer<String, CalculatedRate> calcRateConsumer;

    @BeforeEach
    void setupConsumers() {
        rawRateConsumer = createConsumer(RawRate.class);
        rawRateConsumer.subscribe(Collections.singletonList(rawRateTopic));

        calcRateConsumer = createConsumer(CalculatedRate.class);
        calcRateConsumer.subscribe(Collections.singletonList(calcRateTopic));
    }

    private <T> Consumer<String, T> createConsumer(Class<T> valueType) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, String.format("test-group-%s", valueType.getSimpleName()));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new KafkaConsumer<>(props);
    }

    @Test
    void testRawRateKafkaProducer() {
        RawRate rawRate = rateFactory.createRawRate("USD_TRY", "CNN_TCP", 35.0, 34.0, Instant.now());

        kafkaRawRateTemplate.send(rawRateTopic, rawRate);

        ConsumerRecords<String, RawRate> records = KafkaTestUtils.getRecords(rawRateConsumer, Duration.ofSeconds(5));

        assertThat(records.count()).isGreaterThan(0);
        assertThat(records.iterator().next().value().toString()).isEqualTo(rawRate.toString());
    }

    @Test
    void testCalcRateKafkaProducer() {
        CalculatedRate calcRate = rateFactory.createCalcRate("USD_TRY", 35.0, 34.0, Instant.now());

        kafkaCalcRateTemplate.send(calcRateTopic, calcRate);

        ConsumerRecords<String, CalculatedRate> records = KafkaTestUtils.getRecords(calcRateConsumer, Duration.ofSeconds(5));

        assertThat(records.count()).isGreaterThan(0);
        assertThat(records.iterator().next().value().toString()).isEqualTo(calcRate.toString());
    }
}
