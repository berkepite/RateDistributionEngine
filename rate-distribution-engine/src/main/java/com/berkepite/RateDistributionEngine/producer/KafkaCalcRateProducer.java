package com.berkepite.RateDistributionEngine.producer;

import com.berkepite.RateDistributionEngine.common.rate.CalculatedRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Producer service responsible for sending calculated rate events to Kafka.
 */
@Service
public class KafkaCalcRateProducer {

    private final KafkaTemplate<String, CalculatedRate> kafkaCalcRateTemplate;

    /**
     * Constructs the KafkaCalcRateProducer with the KafkaTemplate injected.
     *
     * @param kafkaCalcRateTemplate The Kafka template configured to send CalculatedRate messages.
     */
    @Autowired
    public KafkaCalcRateProducer(KafkaTemplate<String, CalculatedRate> kafkaCalcRateTemplate) {
        this.kafkaCalcRateTemplate = kafkaCalcRateTemplate;
    }

    /**
     * Sends a calculated rate message to the default Kafka topic.
     *
     * @param rate The calculated rate to be sent.
     */
    public void sendCalcRate(CalculatedRate rate) {
        kafkaCalcRateTemplate.sendDefault(rate);
    }
}
