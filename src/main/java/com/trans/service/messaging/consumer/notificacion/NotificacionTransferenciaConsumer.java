package com.trans.service.messaging.consumer.notificacion;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.trans.service.dto.TransferenciaRealizadaEvent;
import com.trans.service.services.NotificacionTransferenciaService;

/** Dominio: notificaciones. Consume transferencia.realizada desde la cola notificaciones.transferencia. */
@Component
public class NotificacionTransferenciaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionTransferenciaConsumer.class);

    private final NotificacionTransferenciaService notificacionService;

    public NotificacionTransferenciaConsumer(NotificacionTransferenciaService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-notificaciones}")
    public void onTransferenciaRealizada(@Payload TransferenciaRealizadaEvent evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag,
            @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            if (evento == null || evento.idTransaccion() == null) {
                throw new IllegalArgumentException("El evento no trae idTransaccion");
            }
            notificacionService.notificar(evento);
            channel.basicAck(tag, false);                               // procesado -> ACK
        } catch (IllegalArgumentException e) {
            // Error NO recuperable (mensaje inválido): reintentar no sirve -> DLQ
            log.error("[NOTIFICACION] Mensaje inválido, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        } catch (Exception e) {
            if (!redelivered) {
                // Error recuperable: se reencola una sola vez
                log.warn("[NOTIFICACION] Fallo temporal, se reintenta una vez: {}", e.getMessage());
                channel.basicNack(tag, false, true);
            } else {
                // Ya se reintentó -> DLQ
                log.error("[NOTIFICACION] Fallo tras reintento, enviado a DLQ: {}", e.getMessage());
                channel.basicNack(tag, false, false);
            }
        }
    }
}
