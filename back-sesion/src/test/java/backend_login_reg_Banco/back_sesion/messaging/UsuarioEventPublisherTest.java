package backend_login_reg_Banco.back_sesion.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import backend_login_reg_Banco.back_sesion.config.RabbitMQProperties;
import backend_login_reg_Banco.back_sesion.model.Usuario;

@ExtendWith(MockitoExtension.class)
class UsuarioEventPublisherTest {

    @Mock
    private RabbitTemplate rabbit;

    private final RabbitMQProperties props = new RabbitMQProperties(
            "banco.events", "banco.dlx", "usuario.registrado",
            "notificaciones.usuario", "notificaciones.usuario.dlq");

    private Usuario usuario() {
        return Usuario.builder().idUsuario(1L).nombre("Ana").rut("1-9").gmail("ana@mail.com").build();
    }

    @Test
    void publicaElEventoEnElExchangeCorrecto() {
        new UsuarioEventPublisher(rabbit, props).publicar(usuario());

        verify(rabbit).convertAndSend(
                eq("banco.events"), eq("usuario.registrado"), any(Object.class));
    }

    @Test
    void noPropagaLaExcepcionSiRabbitFalla() {
        doThrow(new AmqpException("RabbitMQ caído"))
                .when(rabbit).convertAndSend(any(String.class), any(String.class), any(Object.class));
        UsuarioEventPublisher publisher = new UsuarioEventPublisher(rabbit, props);

        assertDoesNotThrow(() -> publisher.publicar(usuario()));
    }
}
