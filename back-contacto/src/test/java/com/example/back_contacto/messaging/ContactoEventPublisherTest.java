package com.example.back_contacto.messaging;

import com.example.back_contacto.config.RabbitMQProperties;
import com.example.back_contacto.messaging.event.ContactoEvent;
import com.example.back_contacto.messaging.producer.ContactoEventPublisher;
import com.example.back_contacto.model.Contacto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContactoEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQProperties props;
    private ContactoEventPublisher publisher;
    private Contacto contacto;

    @BeforeEach
    void setUp() {
        props = new RabbitMQProperties();
        props.setExchange("banco.events");
        props.getContactoCreado().setRoutingKey("contacto.creado");
        publisher = new ContactoEventPublisher(rabbitTemplate, props);

        contacto = new Contacto();
        contacto.setIdContacto(7L);
        contacto.setIdUsuario(1L);
        contacto.setNombreContacto("Ana");
        contacto.setGmail("ana@gmail.com");
        contacto.setRut("12345678-9");
    }

    @Test
    void publicaEventoEnExchangeConRoutingKeyConfigurada() {
        publisher.publicarContactoCreado(contacto);

        ArgumentCaptor<ContactoEvent> captor = ArgumentCaptor.forClass(ContactoEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("banco.events"), eq("contacto.creado"), captor.capture());
        assertEquals("CONTACTO_CREADO", captor.getValue().getTipo());
        assertEquals(7L, captor.getValue().getIdContacto());
    }

    @Test
    void siRabbitFallaNoLanzaExcepcion() {
        doThrow(new AmqpConnectException(new RuntimeException("broker caído")))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

        assertDoesNotThrow(() -> publisher.publicarContactoCreado(contacto));
    }
}
