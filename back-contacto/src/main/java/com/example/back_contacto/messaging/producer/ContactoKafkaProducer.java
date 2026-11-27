package com.example.back_contacto.messaging.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.back_contacto.config.KafkaTopicsProperties;
import com.example.back_contacto.messaging.event.ContactoEvent;

@Component
public class ContactoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(ContactoKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties props;

    public ContactoKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicsProperties props) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
    }

    public void publicar(ContactoEvent evento) {
        String topic = props.topicContactos();
        String key = String.valueOf(evento.getIdContacto());
        try {
            org.apache.kafka.clients.producer.ProducerRecord<String, Object> record = new org.apache.kafka.clients.producer.ProducerRecord<>(topic, key, evento);
            record.headers().add("event-type", "contacto.creado".getBytes());

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA PRODUCER] Evento de contacto {} enviado exitosamente a topic {} [partition: {}, offset: {}]",
                            evento.getIdContacto(), topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA PRODUCER] Error enviando evento de contacto {} a topic {}: {}",
                            evento.getIdContacto(), topic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("[KAFKA PRODUCER] Excepción al enviar contacto {} a Kafka: {}", evento.getIdContacto(), e.getMessage());
        }
    }
}
