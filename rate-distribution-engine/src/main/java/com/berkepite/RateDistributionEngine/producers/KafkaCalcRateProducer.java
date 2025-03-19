package com.berkepite.RateDistributionEngine.producers;

import com.berkepite.RateDistributionEngine.common.rates.CalculatedRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaCalcRateProducer {
    private final KafkaTemplate<String, CalculatedRate> kafkaCalcRateTemplate;

    @Autowired
    KafkaCalcRateProducer(KafkaTemplate<String, CalculatedRate> kafkaCalcRateTemplate) {
        this.kafkaCalcRateTemplate = kafkaCalcRateTemplate;
    }

    public void sendCalcRate(CalculatedRate rate) {
        kafkaCalcRateTemplate.sendDefault(rate);
    }
}
