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
    private final TransferenciaKafkaProducer kafkaProducer;

    public TransferenciaEventPublisher(RabbitTemplate rabbit, RabbitMQProperties p, TransferenciaKafkaProducer kafkaProducer) {
        this.rabbit = rabbit;
        this.p = p;
        this.kafkaProducer = kafkaProducer;
    }

    public void publicar(Transaction t) {
        try {
            var evento = new TransferenciaRealizadaEvent(
                    t.getIdTransferencia(), t.getMonto(), LocalDateTime.now());
            
            // 1. RabbitMQ (Flujo existente intacto)
            rabbit.convertAndSend(p.exchange(), p.routingTransferencia(), evento);
            log.info("Evento publicado en RabbitMQ: transferencia {}", t.getIdTransferencia());

            // 2. Kafka (En paralelo, protegido por try/catch para no romper RabbitMQ)
            try {
                kafkaProducer.publicar(evento);
            } catch (Exception e) {
                log.error("[KAFKA ERROR] No se pudo publicar en Kafka para transferencia {}: {}. RabbitMQ sigue funcionando.",
                        t.getIdTransferencia(), e.getMessage());
            }

        } catch (Exception e) {
            log.error("No se pudo publicar la transferencia {}: {}", t.getIdTransferencia(), e.getMessage());
        }
    }
}
