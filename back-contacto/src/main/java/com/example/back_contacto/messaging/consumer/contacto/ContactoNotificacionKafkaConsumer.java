package com.example.back_contacto.messaging.consumer.contacto;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.back_contacto.messaging.event.ContactoEvent;
import com.example.back_contacto.service.NotificacionContactoService;

@Component
public class ContactoNotificacionKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ContactoNotificacionKafkaConsumer.class);

    private final NotificacionContactoService service;

    public ContactoNotificacionKafkaConsumer(NotificacionContactoService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${app.kafka.topic-usuarios}", groupId = "notificaciones-contacto-group")
    public void consumir(@Payload ContactoEvent evento, ConsumerRecord<String, Object> record) {
        log.info("[KAFKA CONSUMER - CONTACTOS] Topic: {}, Partition: {}, Offset: {}, Key: {}",
                record.topic(), record.partition(), record.offset(), record.key());

        if (evento == null || evento.getIdContacto() == null || evento.getGmail() == null) {
            log.error("[KAFKA VALIDATION ERROR] Payload de contacto inválido o nulo: {}", evento);
            throw new IllegalArgumentException("Payload de contacto inválido o nulo");
        }

        service.notificarContactoCreado(evento);
    }
}
