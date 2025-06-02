package com.berkepite.RateDistributionEngine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor coordinatorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);        // Initial pool size
        executor.setMaxPoolSize(24);       // Maximum pool size
        executor.setQueueCapacity(100);   // Capacity of the queue for waiting tasks
        executor.setThreadNamePrefix("coordinator-task-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor subscriberExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);      // Initial pool size
        executor.setMaxPoolSize(200);     // Maximum pool size
        executor.setQueueCapacity(100);  // Capacity of the queue for waiting tasks
        executor.setThreadNamePrefix("subscriber-task-");
        executor.initialize();
        return executor;
    }
}