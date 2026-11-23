package com.trans.service.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.trans.service.config.KafkaTopicsProperties;
import com.trans.service.dto.TransferenciaRealizadaEvent;

@Component
public class TransferenciaKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties props;

    public TransferenciaKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicsProperties props) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
    }

    public void publicar(TransferenciaRealizadaEvent evento) {
        String topic = props.topicTransacciones();
        String key = String.valueOf(evento.idTransaccion());
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, evento);
            record.headers().add("event-type", "transferencia.realizada".getBytes());

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA PRODUCER] Evento de transferencia {} enviado exitosamente a topic {} [partition: {}, offset: {}]",
                            evento.idTransaccion(), topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA PRODUCER] Error enviando evento de transferencia {} a topic {}: {}",
                            evento.idTransaccion(), topic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("[KAFKA PRODUCER] Excepción al enviar transferencia {} a Kafka: {}", evento.idTransaccion(), e.getMessage());
        }
    }
}
