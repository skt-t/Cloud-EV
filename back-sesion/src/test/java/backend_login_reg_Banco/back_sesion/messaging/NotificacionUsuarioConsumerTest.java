package backend_login_reg_Banco.back_sesion.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;
import backend_login_reg_Banco.back_sesion.dto.UsuarioRegistradoEvent;
import backend_login_reg_Banco.back_sesion.messaging.consumer.notificacion.NotificacionUsuarioConsumer;
import backend_login_reg_Banco.back_sesion.service.NotificacionUsuarioService;

@ExtendWith(MockitoExtension.class)
class NotificacionUsuarioConsumerTest {

    @Mock
    private Channel channel;
    @Mock
    private NotificacionUsuarioService servicio;

    private UsuarioRegistradoEvent valido() {
        return new UsuarioRegistradoEvent(1L, "Ana", "ana@mail.com", LocalDateTime.now());
    }

    @Test
    void eventoValidoHaceAck() throws IOException {
        new NotificacionUsuarioConsumer(servicio).onUsuarioRegistrado(valido(), channel, 5L, false);

        verify(servicio).notificarBienvenida(any());
        verify(channel).basicAck(5L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void eventoNuloVaALaDlqSinReencolar() throws IOException {
        new NotificacionUsuarioConsumer(servicio).onUsuarioRegistrado(null, channel, 6L, false);

        verify(channel).basicNack(6L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(servicio, never()).notificarBienvenida(any());
    }

    @Test
    void eventoSinIdVaALaDlqSinReencolar() throws IOException {
        UsuarioRegistradoEvent sinId = new UsuarioRegistradoEvent(null, "Ana", "ana@mail.com", LocalDateTime.now());

        new NotificacionUsuarioConsumer(servicio).onUsuarioRegistrado(sinId, channel, 7L, false);

        verify(channel).basicNack(7L, false, false);
    }

    @Test
    void falloTemporalReencolaUnaVezYLuegoVaADlq() throws IOException {
        doThrow(new RuntimeException("fallo")).when(servicio).notificarBienvenida(any());
        NotificacionUsuarioConsumer consumer = new NotificacionUsuarioConsumer(servicio);

        consumer.onUsuarioRegistrado(valido(), channel, 8L, false);
        verify(channel).basicNack(8L, false, true);

        consumer.onUsuarioRegistrado(valido(), channel, 9L, true);
        verify(channel).basicNack(9L, false, false);
    }
}
