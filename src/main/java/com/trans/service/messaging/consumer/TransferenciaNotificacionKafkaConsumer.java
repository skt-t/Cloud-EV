package com.trans.service.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.trans.service.dto.TransferenciaRealizadaEvent;
import com.trans.service.services.NotificacionTransferenciaService;

@Component
public class TransferenciaNotificacionKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaNotificacionKafkaConsumer.class);

    private final NotificacionTransferenciaService service;

    public TransferenciaNotificacionKafkaConsumer(NotificacionTransferenciaService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${app.kafka.topic-transacciones}", groupId = "notificaciones-transferencia-group")
    public void consumir(@Payload TransferenciaRealizadaEvent evento, ConsumerRecord<String, Object> record) {
        log.info("[KAFKA CONSUMER - NOTIFICACIONES] Topic: {}, Partition: {}, Offset: {}, Key: {}",
                record.topic(), record.partition(), record.offset(), record.key());

        // Validación de payload
        if (evento == null || evento.idTransaccion() == null || evento.monto() == null || evento.monto().signum() <= 0) {
            log.error("[KAFKA VALIDATION ERROR] Payload inválido recibido en notificaciones: {}", evento);
            throw new IllegalArgumentException("Payload de transferencia inválido o nulo");
        }

        service.notificar(evento);
    }
}
