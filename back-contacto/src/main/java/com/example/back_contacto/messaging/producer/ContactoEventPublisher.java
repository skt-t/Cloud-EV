package com.example.back_contacto.messaging.producer;

import com.example.back_contacto.config.RabbitMQProperties;
import com.example.back_contacto.messaging.event.ContactoEvent;
import com.example.back_contacto.model.Contacto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Productor: publica eventos de contacto en el exchange.
 * Si RabbitMQ no está disponible solo se registra el error, para no afectar la lógica existente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContactoEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public void publicarContactoCreado(Contacto contacto) {
        publicar(props.getContactoCreado().getRoutingKey(), "CONTACTO_CREADO", contacto);
    }

    private void publicar(String routingKey, String tipo, Contacto contacto) {
        ContactoEvent evento = ContactoEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .tipo(tipo)
                .idContacto(contacto.getIdContacto())
                .idUsuario(contacto.getIdUsuario())
                .nombreContacto(contacto.getNombreContacto())
                .gmail(contacto.getGmail())
                .rut(contacto.getRut())
                .fecha(Instant.now())
                .build();
        try {
            rabbitTemplate.convertAndSend(props.getExchange(), routingKey, evento);
            log.info("[RABBITMQ] Evento {} publicado (routingKey={}, idContacto={})",
                    tipo, routingKey, contacto.getIdContacto());
        } catch (Exception e) {
            log.error("[RABBITMQ] No se pudo publicar {} (idContacto={}): {}",
                    tipo, contacto.getIdContacto(), e.getMessage());
        }
    }
}
