package com.trans.service.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;
import com.trans.service.dto.TransferenciaRealizadaEvent;
import com.trans.service.messaging.consumer.notificacion.NotificacionTransferenciaConsumer;
import com.trans.service.services.NotificacionTransferenciaService;

@ExtendWith(MockitoExtension.class)
class NotificacionTransferenciaConsumerTest {

    @Mock
    private Channel channel;
    @Mock
    private NotificacionTransferenciaService servicio;

    private TransferenciaRealizadaEvent valido() {
        return new TransferenciaRealizadaEvent(1L, BigDecimal.TEN, LocalDateTime.now());
    }

    @Test
    void eventoValidoHaceAck() throws IOException {
        new NotificacionTransferenciaConsumer(servicio).onTransferenciaRealizada(valido(), channel, 5L, false);

        verify(servicio).notificar(any());
        verify(channel).basicAck(5L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void eventoNuloVaALaDlqSinReencolar() throws IOException {
        new NotificacionTransferenciaConsumer(servicio).onTransferenciaRealizada(null, channel, 6L, false);

        verify(channel).basicNack(6L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(servicio, never()).notificar(any());
    }

    @Test
    void eventoSinIdVaALaDlqSinReencolar() throws IOException {
        TransferenciaRealizadaEvent sinId =
                new TransferenciaRealizadaEvent(null, BigDecimal.TEN, LocalDateTime.now());

        new NotificacionTransferenciaConsumer(servicio).onTransferenciaRealizada(sinId, channel, 7L, false);

        verify(channel).basicNack(7L, false, false);
    }

    @Test
    void falloTemporalReencolaUnaVezYLuegoVaADlq() throws IOException {
        doThrow(new RuntimeException("fallo")).when(servicio).notificar(any());
        NotificacionTransferenciaConsumer consumer = new NotificacionTransferenciaConsumer(servicio);

        consumer.onTransferenciaRealizada(valido(), channel, 8L, false);
        verify(channel).basicNack(8L, false, true);

        consumer.onTransferenciaRealizada(valido(), channel, 9L, true);
        verify(channel).basicNack(9L, false, false);
    }
}
