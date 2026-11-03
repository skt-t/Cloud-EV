package com.trans.service.messaging;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.trans.service.config.RabbitMQProperties;
import com.trans.service.dto.TransferenciaRealizadaEvent;
import com.trans.service.entities.Transaction;

@Component
public class TransferenciaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaEventPublisher.class);

    private final RabbitTemplate rabbit;
    private final RabbitMQProperties p;

    public TransferenciaEventPublisher(RabbitTemplate rabbit, RabbitMQProperties p) {
        this.rabbit = rabbit;
        this.p = p;
    }

    public void publicar(Transaction t) {
        try {
            var evento = new TransferenciaRealizadaEvent(
                    t.getIdTransferencia(), t.getMonto(), LocalDateTime.now());
            rabbit.convertAndSend(p.exchange(), p.routingTransferencia(), evento);
            log.info("Evento publicado: transferencia {}", t.getIdTransferencia());
        } catch (Exception e) {
            log.error("No se pudo publicar la transferencia {}: {}", t.getIdTransferencia(), e.getMessage());
        }
    }
}
