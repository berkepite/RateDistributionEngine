package com.berkepite.MainApplication32Bit.rates;

import com.berkepite.MainApplication32Bit.subscribers.SubscriberEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface IRateRepository extends JpaRepository<RateEntity, Long> {

    Optional<RateEntity> findByProviderAndRate(SubscriberEnum provider, RateEnum rate);

    Optional<RateEntity> findByProviderAndRateAndTimestamp(SubscriberEnum provider, RateEnum rate, Instant timestamp);

}