package com.example.back_contacto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaTopicsProperties(
        String topicContactos,
        int partitions,
        int replicas,
        long retentionMs,
        String cleanupPolicy,
        int minInsyncReplicas) {
}
