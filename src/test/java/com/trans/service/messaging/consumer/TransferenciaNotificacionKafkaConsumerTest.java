package com.trans.service.messaging.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trans.service.dto.TransferenciaRealizadaEvent;
import com.trans.service.services.NotificacionTransferenciaService;

@ExtendWith(MockitoExtension.class)
class TransferenciaNotificacionKafkaConsumerTest {

    @Mock
    private NotificacionTransferenciaService service;

    @InjectMocks
    private TransferenciaNotificacionKafkaConsumer consumer;

    private ConsumerRecord<String, Object> record(TransferenciaRealizadaEvent evento) {
        return new ConsumerRecord<>("banco.transacciones.events", 0, 0L, "55", evento);
    }

    @Test
    void consumir_eventoValido_delegaAlServicio() {
        TransferenciaRealizadaEvent evento =
                new TransferenciaRealizadaEvent(55L, new BigDecimal("15000"), LocalDateTime.now());

        consumer.consumir(evento, record(evento));

        verify(service).notificar(evento);
    }

    @Test
    void consumir_montoCero_lanzaExcepcionParaIrAlDlt() {
        TransferenciaRealizadaEvent evento =
                new TransferenciaRealizadaEvent(55L, BigDecimal.ZERO, LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(evento, record(evento)));
        verifyNoInteractions(service);
    }

    @Test
    void consumir_montoNulo_lanzaExcepcion() {
        TransferenciaRealizadaEvent evento = new TransferenciaRealizadaEvent(55L, null, LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(evento, record(evento)));
        verifyNoInteractions(service);
    }

    @Test
    void consumir_idNulo_lanzaExcepcion() {
        TransferenciaRealizadaEvent evento =
                new TransferenciaRealizadaEvent(null, new BigDecimal("100"), LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(evento, record(evento)));
        verifyNoInteractions(service);
    }

    @Test
    void consumir_payloadNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(null, record(null)));
        verifyNoInteractions(service);
    }
}