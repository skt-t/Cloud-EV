package com.trans.service.messaging;

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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.rabbitmq.client.Channel;
import com.trans.service.dto.TransferenciaRealizadaEvent;

@ExtendWith(MockitoExtension.class)
class TransferenciaConsumerTest {

    @Mock
    private Channel channel;

    private final TransferenciaConsumer consumer = new TransferenciaConsumer();

    private Message mensaje() {
        MessageProperties props = new MessageProperties();
        props.setRedelivered(false);
        return new Message(new byte[0], props);
    }

    private TransferenciaRealizadaEvent eventoValido() {
        return new TransferenciaRealizadaEvent(1L, BigDecimal.TEN, LocalDateTime.now());
    }

    @Test
    void eventoValidoHaceAck() throws Exception {
        consumer.notificar(eventoValido(), mensaje(), channel, 5L);

        verify(channel).basicAck(5L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void eventoNuloVaALaDlq() throws Exception {
        consumer.notificar(null, mensaje(), channel, 5L);

        verify(channel).basicNack(5L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void eventoSinIdVaALaDlq() throws Exception {
        TransferenciaRealizadaEvent sinId =
                new TransferenciaRealizadaEvent(null, BigDecimal.TEN, LocalDateTime.now());

        consumer.comprobante(sinId, mensaje(), channel, 7L);

        verify(channel).basicNack(7L, false, false);
    }

    @Test
    void falloTemporalReintentaUnaVez() throws Exception {
        doThrow(new IOException("fallo")).when(channel).basicAck(5L, false);

        consumer.notificar(eventoValido(), mensaje(), channel, 5L);

        verify(channel).basicNack(5L, false, true);
    }
}
