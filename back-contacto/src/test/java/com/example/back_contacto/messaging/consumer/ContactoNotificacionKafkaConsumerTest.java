package com.example.back_contacto.messaging.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.back_contacto.messaging.consumer.contacto.ContactoNotificacionKafkaConsumer;
import com.example.back_contacto.messaging.event.ContactoEvent;
import com.example.back_contacto.service.NotificacionContactoService;

@ExtendWith(MockitoExtension.class)
class ContactoNotificacionKafkaConsumerTest {

    @Mock
    private NotificacionContactoService service;

    @InjectMocks
    private ContactoNotificacionKafkaConsumer consumer;

    private ConsumerRecord<String, Object> record(ContactoEvent evento) {
        return new ConsumerRecord<>("banco.contactos.events", 0, 0L, "10", evento);
    }

    @Test
    void consumir_eventoValido_delegaAlServicio() {
        ContactoEvent evento = ContactoEvent.builder()
                .idContacto(10L).gmail("ana@gmail.com").build();

        consumer.consumir(evento, record(evento));

        verify(service).notificarContactoCreado(evento);
    }

    @Test
    void consumir_eventoSinGmail_lanzaExcepcionParaIrAlDlt() {
        ContactoEvent evento = ContactoEvent.builder().idContacto(10L).build();

        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(evento, record(evento)));
        verifyNoInteractions(service);
    }

    @Test
    void consumir_eventoSinIdContacto_lanzaExcepcion() {
        ContactoEvent evento = ContactoEvent.builder().gmail("ana@gmail.com").build();

        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(evento, record(evento)));
        verifyNoInteractions(service);
    }

    @Test
    void consumir_payloadNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> consumer.consumir(null, record(null)));
        verifyNoInteractions(service);
    }
}