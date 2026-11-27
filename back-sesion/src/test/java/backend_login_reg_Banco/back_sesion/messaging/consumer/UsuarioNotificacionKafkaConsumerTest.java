package backend_login_reg_Banco.back_sesion.messaging.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend_login_reg_Banco.back_sesion.dto.UsuarioRegistradoEvent;
import backend_login_reg_Banco.back_sesion.service.NotificacionUsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioNotificacionKafkaConsumerTest {

    @Mock
    private NotificacionUsuarioService service;

    @InjectMocks
    private UsuarioNotificacionKafkaConsumer consumer;

    private ConsumerRecord<String, Object> record(UsuarioRegistradoEvent evento) {
        return new ConsumerRecord<>("banco.usuarios.events", 0, 0L, "1", evento);
    }

    @Test
    void consumir_eventoValido_delegaAlServicio() {
        UsuarioRegistradoEvent evento = new UsuarioRegistradoEvent(1L, "Ana", "ana@gmail.com", LocalDateTime.now());

        consumer.consumir(evento, record(evento));

        verify(service).notificarBienvenida(evento);
    }

    @Test
    void consumir_eventoSinGmail_lanzaExcepcionParaIrAlDlt() {
        UsuarioRegistradoEvent evento = new UsuarioRegistradoEvent(1L, "Ana", null, LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(evento, record(evento)));
        verifyNoInteractions(service);
    }

    @Test
    void consumir_eventoSinIdUsuario_lanzaExcepcion() {
        UsuarioRegistradoEvent evento = new UsuarioRegistradoEvent(null, "Ana", "ana@gmail.com", LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(evento, record(evento)));
        verifyNoInteractions(service);
    }

    @Test
    void consumir_payloadNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(null, record(null)));
        verifyNoInteractions(service);
    }
}