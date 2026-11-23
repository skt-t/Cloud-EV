package com.trans.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaTopicsProperties(
        String topicTransacciones,
        int partitions,
        int replicas,
        long retentionMs,
        String cleanupPolicy,
        int minInsyncReplicas) {
}
