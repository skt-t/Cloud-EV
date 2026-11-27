package com.example.back_contacto.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@Configuration
@EnableConfigurationProperties({KafkaTopicsProperties.class, KafkaProperties.class})
public class KafkaConfig {

    private final KafkaTopicsProperties props;
    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaTopicsProperties props, KafkaProperties kafkaProperties) {
        this.props = props;
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public NewTopic contactosTopic() {
        return TopicBuilder.name(props.topicContactos())
                .partitions(props.partitions())
                .replicas(props.replicas())
                .config("retention.ms", String.valueOf(props.retentionMs()))
                .config("cleanup.policy", props.cleanupPolicy())
                .config("min.insync.replicas", String.valueOf(props.minInsyncReplicas()))
                .build();
    }

    @Bean
    public NewTopic contactosDltTopic() {
    return TopicBuilder.name(props.topicContactos() + ".DLT")
                .partitions(props.partitions())
                .replicas(props.replicas())
                .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> propsMap = kafkaProperties.buildProducerProperties();
        propsMap.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        propsMap.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(propsMap);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, byte[]> byteArrayKafkaTemplate() {
        Map<String, Object> propsMap = kafkaProperties.buildProducerProperties();
        propsMap.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        propsMap.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        ProducerFactory<String, byte[]> pf = new DefaultKafkaProducerFactory<>(propsMap);
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                            KafkaTemplate<String, byte[]> byteArrayKafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                Map.of(
                        byte[].class, byteArrayKafkaTemplate,
                        Object.class, kafkaTemplate
                ),
                (record, ex) -> new org.apache.kafka.common.TopicPartition(props.topicContactos() + ".DLT", record.partition())
        );

        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxInterval(10000L);
        backOff.setMaxElapsedTime(30000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(
                SerializationException.class,
                IllegalArgumentException.class,
                NullPointerException.class
        );
        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler);
        factory.setRecordFilterStrategy(record -> {
            var header = record.headers().lastHeader("event-type");
            if (header != null) {
                String type = new String(header.value());
                return !type.equals("contacto.creado");
            }
            return false;
        });
        factory.setAckDiscarded(true);
        return factory;
    }
}
