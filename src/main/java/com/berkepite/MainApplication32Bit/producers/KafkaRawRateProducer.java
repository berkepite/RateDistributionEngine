package com.berkepite.MainApplication32Bit.producers;

import com.berkepite.MainApplication32Bit.rates.RawRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaRawRateProducer {
    private final KafkaTemplate<String, RawRate> kafkaRawRateTemplate;

    @Autowired
    KafkaRawRateProducer(KafkaTemplate<String, RawRate> kafkaRawRateTemplate) {
        this.kafkaRawRateTemplate = kafkaRawRateTemplate;
    }

    public void sendRawRate(RawRate rate) {
        kafkaRawRateTemplate.sendDefault(rate);
    }
}
