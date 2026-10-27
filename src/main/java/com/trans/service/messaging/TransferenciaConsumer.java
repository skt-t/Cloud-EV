package com.trans.service.messaging;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.trans.service.dto.TransferenciaRealizadaEvent;

@Component
public class TransferenciaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue-notificaciones}")
    public void notificar(TransferenciaRealizadaEvent e, Message m, Channel ch,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesar(e, m, ch, tag, "notificación");
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-comprobante}")
    public void comprobante(TransferenciaRealizadaEvent e, Message m, Channel ch,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesar(e, m, ch, tag, "comprobante");
    }

    private void procesar(TransferenciaRealizadaEvent e, Message m, Channel ch,
            long tag, String tipo) throws IOException {
        try {
            if (e == null || e.idTransaccion() == null) {
                log.warn("Mensaje inválido ({}), enviado a DLQ", tipo);
                ch.basicNack(tag, false, false);
                return;
            }
            log.info("Generando {} de la transferencia {}", tipo, e.idTransaccion());
            ch.basicAck(tag, false);
        } catch (Exception ex) {
            boolean reintento = !m.getMessageProperties().isRedelivered();
            log.error("Fallo en {} ({}), reintento={}", tipo, ex.getMessage(), reintento);
            ch.basicNack(tag, false, reintento);
        }
    }
}
