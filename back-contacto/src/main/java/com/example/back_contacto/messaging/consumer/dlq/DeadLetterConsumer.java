package com.example.back_contacto.messaging.consumer.dlq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Registra en logs los mensajes no entregados que llegan a la DLQ.
 * app.rabbitmq.dlq-logger.enabled=false deja los mensajes en la DLQ (útil para mostrarlos en la demo).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.rabbitmq.dlq-logger.enabled", havingValue = "true", matchIfMissing = true)
public class DeadLetterConsumer {

    @RabbitListener(queues = "${app.rabbitmq.contacto-creado.dlq}")
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
