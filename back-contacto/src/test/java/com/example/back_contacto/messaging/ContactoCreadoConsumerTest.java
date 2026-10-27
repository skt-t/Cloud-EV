package com.example.back_contacto.messaging;

import com.example.back_contacto.messaging.consumer.contacto.ContactoCreadoConsumer;
import com.example.back_contacto.messaging.event.ContactoEvent;
import com.example.back_contacto.service.NotificacionContactoService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContactoCreadoConsumerTest {

    @Mock
    private Channel channel;
    @Mock
    private NotificacionContactoService notificacionService;

    private ContactoEvent valido() {
        ContactoEvent e = new ContactoEvent();
        e.setIdContacto(1L);
        e.setGmail("ana@gmail.com");
        e.setNombreContacto("Ana");
        return e;
    }

    @Test
    void eventoValidoHaceAck() throws IOException {
        new ContactoCreadoConsumer(notificacionService).onContactoCreado(valido(), channel, 10L, false);

        verify(notificacionService).notificarContactoCreado(any());
        verify(channel).basicAck(10L, false);
    }

    @Test
    void eventoInvalidoVaADlqSinReencolar() throws IOException {
        new ContactoCreadoConsumer(notificacionService).onContactoCreado(new ContactoEvent(), channel, 11L, false);

        verify(channel).basicNack(11L, false, false);
        verify(channel, never()).basicAck(11L, false);
    }

    @Test
    void falloTemporalReencolaUnaVezYLuegoVaADlq() throws IOException {
        doThrow(new RuntimeException("SMTP caído")).when(notificacionService).notificarContactoCreado(any());
        ContactoCreadoConsumer consumer = new ContactoCreadoConsumer(notificacionService);

        consumer.onContactoCreado(valido(), channel, 12L, false);
        verify(channel).basicNack(12L, false, true);

        consumer.onContactoCreado(valido(), channel, 13L, true);
        verify(channel).basicNack(13L, false, false);
    }
}
