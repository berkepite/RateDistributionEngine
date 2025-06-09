package com.berkepite.RateDistributionEngine.producer;

import com.berkepite.RateDistributionEngine.common.rate.RawRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Producer service responsible for sending raw rate events to Kafka.
 */
@Service
public class KafkaRawRateProducer {

    private final KafkaTemplate<String, RawRate> kafkaRawRateTemplate;

    /**
     * Constructs the KafkaRawRateProducer with the KafkaTemplate injected.
     *
     * @param kafkaRawRateTemplate The Kafka template configured to send RawRate messages.
     */
    @Autowired
    public KafkaRawRateProducer(KafkaTemplate<String, RawRate> kafkaRawRateTemplate) {
        this.kafkaRawRateTemplate = kafkaRawRateTemplate;
    }

    /**
     * Sends a raw rate message to the default Kafka topic.
     *
     * @param rate The raw rate to be sent.
     */
    public void sendRawRate(RawRate rate) {
        kafkaRawRateTemplate.sendDefault(rate);
    }
}
