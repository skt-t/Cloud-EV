package com.example.back_contacto.messaging.consumer.contacto;

import com.example.back_contacto.messaging.event.ContactoEvent;
import com.example.back_contacto.service.NotificacionContactoService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Dominio: contactos. Consume contacto.creado desde la cola notificaciones.contacto. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContactoCreadoConsumer {

    private final NotificacionContactoService notificacionService;

    @RabbitListener(queues = "${app.rabbitmq.contacto-creado.queue}")
    public void onContactoCreado(@Payload ContactoEvent evento,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                 @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            if (evento == null || evento.getIdContacto() == null
                    || evento.getGmail() == null || evento.getGmail().isBlank()) {
                throw new IllegalArgumentException("El evento no trae idContacto o gmail");
            }
            notificacionService.notificarContactoCreado(evento);
            channel.basicAck(tag, false);                               // procesado -> ACK
        } catch (IllegalArgumentException e) {
            // Error NO recuperable (mensaje inválido): reintentar no sirve -> DLQ
            log.error("[CONTACTO] Mensaje inválido, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        } catch (Exception e) {
            if (!redelivered) {
                // Error recuperable: se reencola una sola vez
                log.warn("[CONTACTO] Fallo temporal, se reintenta una vez: {}", e.getMessage());
                channel.basicNack(tag, false, true);
            } else {
                // Ya se reintentó -> DLQ
                log.error("[CONTACTO] Fallo tras reintento, enviado a DLQ: {}", e.getMessage());
                channel.basicNack(tag, false, false);
            }
        }
    }
}
