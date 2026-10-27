package com.example.back_contacto.messaging.consumer;

import com.example.back_contacto.messaging.event.ContactoEvent;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Dominio: notificaciones. Consume "contacto creado" y envía la notificación. */
@Slf4j
@Component
public class NotificacionConsumer {

    @RabbitListener(queues = "${app.rabbitmq.contacto-creado.queue}")
    public void onContactoCreado(@Payload ContactoEvent evento,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                 @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            validar(evento);
            enviarNotificacion(evento);
            channel.basicAck(tag, false);                 // OK -> ACK
        } catch (IllegalArgumentException e) {
            // Error NO recuperable (mensaje inválido): reintentar no sirve -> DLQ
            log.error("[NOTIFICACION] Mensaje inválido, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        } catch (Exception e) {
            if (!redelivered) {
                // Error recuperable: un reintento (requeue)
                log.warn("[NOTIFICACION] Fallo temporal, se reintenta una vez: {}", e.getMessage());
                channel.basicNack(tag, false, true);
            } else {
                // Ya se reintentó -> DLQ
                log.error("[NOTIFICACION] Fallo tras reintento, enviado a DLQ: {}", e.getMessage());
                channel.basicNack(tag, false, false);
            }
        }
    }

    private void validar(ContactoEvent evento) {
        if (evento == null || evento.getIdContacto() == null || evento.getGmail() == null || evento.getGmail().isBlank()) {
            throw new IllegalArgumentException("El evento no trae idContacto o gmail");
        }
    }

    private void enviarNotificacion(ContactoEvent evento) {
        // Aquí iría el envío real (correo, SNS, etc.). Se simula con un log.
        log.info("[NOTIFICACION] Contacto '{}' creado. Notificación enviada a {} (idContacto={})",
                evento.getNombreContacto(), evento.getGmail(), evento.getIdContacto());
    }
}
