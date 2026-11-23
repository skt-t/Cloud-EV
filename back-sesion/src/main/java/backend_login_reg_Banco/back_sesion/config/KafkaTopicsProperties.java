package backend_login_reg_Banco.back_sesion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaTopicsProperties(
        String topicUsuarios,
        int partitions,
        int replicas,
        long retentionMs,
        String cleanupPolicy,
        int minInsyncReplicas) {
}
