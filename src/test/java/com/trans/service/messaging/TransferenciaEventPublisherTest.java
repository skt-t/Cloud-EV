package com.trans.service.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.trans.service.config.RabbitMQProperties;
import com.trans.service.entities.Transaction;

@ExtendWith(MockitoExtension.class)
class TransferenciaEventPublisherTest {

    @Mock
    private RabbitTemplate rabbit;

    private final RabbitMQProperties props = new RabbitMQProperties(
            "banco.events", "banco.dlx", "transferencia.realizada",
            "cola.notificaciones", "cola.comprobante",
            "dlq.notificaciones", "dlq.comprobante");

    @Test
    void publicaElEventoEnElExchangeCorrecto() {
        Transaction t = mock(Transaction.class);
        TransferenciaEventPublisher publisher = new TransferenciaEventPublisher(rabbit, props);

        publisher.publicar(t);

        verify(rabbit).convertAndSend(
                eq("banco.events"), eq("transferencia.realizada"), any(Object.class));
    }

    @Test
    void noPropagaLaExcepcionSiRabbitFalla() {
        Transaction t = mock(Transaction.class);
        doThrow(new AmqpException("RabbitMQ caído"))
                .when(rabbit).convertAndSend(any(String.class), any(String.class), any(Object.class));
        TransferenciaEventPublisher publisher = new TransferenciaEventPublisher(rabbit, props);

        assertDoesNotThrow(() -> publisher.publicar(t));
    }
}
