package backend_login_reg_Banco.back_sesion.messaging.producer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import backend_login_reg_Banco.back_sesion.config.KafkaTopicsProperties;
import backend_login_reg_Banco.back_sesion.dto.UsuarioRegistradoEvent;
import backend_login_reg_Banco.back_sesion.messaging.UsuarioKafkaProducer;

@ExtendWith(MockitoExtension.class)
class UsuarioKafkaProducerTest {

    private static final String TOPIC = "banco.usuarios.events";

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private UsuarioKafkaProducer producer;
    private UsuarioRegistradoEvent evento;

    @BeforeEach
    void setUp() {
        KafkaTopicsProperties props = new KafkaTopicsProperties(TOPIC, 3, 3, 604800000L, "delete", 2);
        producer = new UsuarioKafkaProducer(kafkaTemplate, props);
        evento = new UsuarioRegistradoEvent(1L, "Ana", "ana@gmail.com", LocalDateTime.now());
    }

    @Test
    @SuppressWarnings("unchecked")
    void publicar_enviaEventoAlTopicConKeyYHeader() {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(new CompletableFuture<>());

        producer.publicar(evento);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, Object> record = captor.getValue();
        assertEquals(TOPIC, record.topic());
        assertEquals("1", record.key());
        assertSame(evento, record.value());
        assertEquals("usuario.registrado",
                new String(record.headers().lastHeader("event-type").value(), StandardCharsets.UTF_8));
    }

    @Test
    @SuppressWarnings("unchecked")
    void publicar_siKafkaFalla_noPropagaLaExcepcion() {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenThrow(new RuntimeException("broker caído"));

        assertDoesNotThrow(() -> producer.publicar(evento));
    }
}