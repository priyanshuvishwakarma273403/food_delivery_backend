package com.delivery.foodDelivery.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    public static final String SALE_TOPIC = "sale-started";
    public static final String SALE_DLT_TOPIC = "sale-started.DLT";

    @Bean
    public NewTopic saleTopic() {
        return TopicBuilder.name(SALE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic saleDltTopic() {
        return TopicBuilder.name(SALE_DLT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Error Handler with Retry Mechanism and Dead Letter Queue (DLQ)
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<Object, Object> template) {
        // Retry 3 times with a delay of 5 seconds between each retry
        return new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(template),
                new FixedBackOff(5000L, 3L)
        );
    }
}
