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

/** Dominio: tickets/comprobantes. Consume "contacto validado" y genera el ticket. */
@Slf4j
@Component
public class TicketConsumer {

    @RabbitListener(queues = "${app.rabbitmq.contacto-validado.queue}")
    public void onContactoValidado(@Payload ContactoEvent evento,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                   @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            validar(evento);
            generarTicket(evento);
            channel.basicAck(tag, false);                 // OK -> ACK
        } catch (IllegalArgumentException e) {
            log.error("[TICKET] Mensaje inválido, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        } catch (Exception e) {
            if (!redelivered) {
                log.warn("[TICKET] Fallo temporal, se reintenta una vez: {}", e.getMessage());
                channel.basicNack(tag, false, true);
            } else {
                log.error("[TICKET] Fallo tras reintento, enviado a DLQ: {}", e.getMessage());
                channel.basicNack(tag, false, false);
            }
        }
    }

    private void validar(ContactoEvent evento) {
        if (evento == null || evento.getIdContacto() == null || evento.getRut() == null || evento.getRut().isBlank()) {
            throw new IllegalArgumentException("El evento no trae idContacto o rut");
        }
    }

    private void generarTicket(ContactoEvent evento) {
        // Aquí iría la generación real del ticket/documento. Se simula con un log.
        String ticket = "TKT-" + evento.getIdContacto() + "-" + System.currentTimeMillis();
        log.info("[TICKET] Cuenta validada para RUT {}. Ticket generado: {}", evento.getRut(), ticket);
    }
}
