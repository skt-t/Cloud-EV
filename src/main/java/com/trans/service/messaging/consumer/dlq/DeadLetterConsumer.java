package com.trans.service.messaging.consumer.dlq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

/**
 * Registra en logs los mensajes no entregados que llegan a las DLQ y los confirma (ACK).
 * app.rabbitmq.dlq-logger.enabled=false deja los mensajes en la DLQ (útil para mostrarlos en la demo).
 */
@Component
@ConditionalOnProperty(name = "app.rabbitmq.dlq-logger.enabled", havingValue = "true", matchIfMissing = true)
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @RabbitListener(queues = {
            "${app.rabbitmq.dlq-notificaciones}",
            "${app.rabbitmq.dlq-comprobante}" })
    public void onMensajeMuerto(Message message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        String cuerpo = new String(message.getBody(), StandardCharsets.UTF_8);
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        log.error("[DLQ] Mensaje no entregado. cola={}, cuerpo={}, x-death={}",
                message.getMessageProperties().getConsumerQueue(), cuerpo, xDeath);
        channel.basicAck(tag, false);
    }
}
