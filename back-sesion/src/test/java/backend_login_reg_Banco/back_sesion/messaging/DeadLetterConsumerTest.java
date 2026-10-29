package backend_login_reg_Banco.back_sesion.messaging;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.rabbitmq.client.Channel;
import backend_login_reg_Banco.back_sesion.messaging.consumer.dlq.DeadLetterConsumer;

@ExtendWith(MockitoExtension.class)
class DeadLetterConsumerTest {

    @Mock
    private Channel channel;

    @Test
    void registraElMensajeMuertoYLoConfirma() throws IOException {
        MessageProperties props = new MessageProperties();
        props.setConsumerQueue("notificaciones.usuario.dlq");
        Message mensaje = new Message("{\"idUsuario\":1}".getBytes(StandardCharsets.UTF_8), props);

        new DeadLetterConsumer().onMensajeMuerto(mensaje, channel, 9L);

        verify(channel).basicAck(9L, false);
    }
}
