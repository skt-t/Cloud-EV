package com.admin.service.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.web.client.RestClient;

import com.admin.service.config.AdminProperties;
import com.admin.service.dto.BindingRequest;
import com.admin.service.dto.ExchangeRequest;
import com.admin.service.dto.QueueRequest;
import com.admin.service.exception.RecursoNoEncontradoException;
import com.admin.service.exception.RecursoProtegidoException;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    private RabbitAdminService service;

    @BeforeEach
    void setUp() {
        AdminProperties props = new AdminProperties(
                "http://localhost:15672", "banco", "x", "banco.dlx",
                List.of("banco.events", "banco.dlx"),
                List.of("notificaciones.usuario", "tickets.comprobante"));
        service = new RabbitAdminService(amqpAdmin, props, RestClient.builder());
    }

    @Test
    void crearColaWithDlqDeclaraDosColasYUnBinding() {
        service.crearCola(new QueueRequest("mi.cola", true, true));

        verify(amqpAdmin, times(2)).declareQueue(any(Queue.class));
        verify(amqpAdmin).declareBinding(any(Binding.class));
    }

    @Test
    void crearColaSinDlqDeclaraSoloUnaCola() {
        service.crearCola(new QueueRequest("mi.cola", true, false));

        verify(amqpAdmin, times(1)).declareQueue(any(Queue.class));
        verify(amqpAdmin, never()).declareBinding(any(Binding.class));
    }

    @Test
    void crearColaProtegidaFalla() {
        assertThrows(RecursoProtegidoException.class,
                () -> service.crearCola(new QueueRequest("tickets.comprobante", true, false)));
        verify(amqpAdmin, never()).declareQueue(any(Queue.class));
    }

    @Test
    void eliminarColaProtegidaFalla() {
        assertThrows(RecursoProtegidoException.class, () -> service.eliminarCola("notificaciones.usuario"));
        verify(amqpAdmin, never()).deleteQueue(any(String.class));
    }

    @Test
    void eliminarColaInexistenteDaNoEncontrado() {
        when(amqpAdmin.getQueueProperties("fantasma")).thenReturn(null);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminarCola("fantasma"));
    }

    @Test
    void eliminarColaExistenteLaBorra() {
        when(amqpAdmin.getQueueProperties("mi.cola")).thenReturn(new Properties());

        service.eliminarCola("mi.cola");

        verify(amqpAdmin).deleteQueue("mi.cola");
    }

    @Test
    void eliminarExchangeProtegidoFalla() {
        assertThrows(RecursoProtegidoException.class, () -> service.eliminarExchange("banco.events"));
    }

    @Test
    void crearExchangeNuevoLoDeclara() {
        service.crearExchange(new ExchangeRequest("ex.demo", "topic", true));

        verify(amqpAdmin).declareExchange(any());
    }

    @Test
    void crearBindingWithColaInexistenteDaNoEncontrado() {
        when(amqpAdmin.getQueueProperties("fantasma")).thenReturn(null);

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.crearBinding(new BindingRequest("ex.demo", "fantasma", "x.y")));
    }
}
