package com.admin.service.service;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.admin.service.config.AdminProperties;
import com.admin.service.dto.BindingRequest;
import com.admin.service.dto.ExchangeRequest;
import com.admin.service.dto.QueueRequest;
import com.admin.service.exception.RecursoNoEncontradoException;
import com.admin.service.exception.RecursoProtegidoException;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA =
            new ParameterizedTypeReference<>() { };

    private final AmqpAdmin admin;
    private final AdminProperties props;
    private final RestClient rest;

    public RabbitAdminService(AmqpAdmin admin, AdminProperties props, RestClient.Builder builder) {
        this.admin = admin;
        this.props = props;
        this.rest = builder
                .baseUrl(props.managementUrl())
                .defaultHeaders(h -> h.setBasicAuth(props.managementUser(), props.managementPassword()))
                .build();
    }

    // ---------- Colas ----------

    public Map<String, Object> crearCola(QueueRequest r) {
        validarColaNoProtegida(r.name());
        QueueBuilder builder = r.durable() ? QueueBuilder.durable(r.name()) : QueueBuilder.nonDurable(r.name());
        if (r.withDlq()) {
            String dlq = r.name() + ".dlq";
            validarColaNoProtegida(dlq);
            DirectExchange dlx = new DirectExchange(props.dlx(), true, false);
            admin.declareExchange(dlx);
            Queue colaDlq = QueueBuilder.durable(dlq).build();
            admin.declareQueue(colaDlq);
            admin.declareBinding(BindingBuilder.bind(colaDlq).to(dlx).with(dlq));
            builder.deadLetterExchange(props.dlx()).deadLetterRoutingKey(dlq);
        }
        admin.declareQueue(builder.build());
        log.info("[ADMIN] Cola creada: {} (dlq={})", r.name(), r.withDlq());
        return Map.of("queue", r.name(), "durable", r.durable(), "dlq", r.withDlq());
    }

    public void eliminarCola(String name) {
        validarColaNoProtegida(name);
        if (admin.getQueueProperties(name) == null) {
            throw new RecursoNoEncontradoException("La cola '" + name + "' no existe");
        }
        admin.deleteQueue(name);
        log.info("[ADMIN] Cola eliminada: {}", name);
    }

    public List<Map<String, Object>> listarColas() {
        return filtrar(obtener("/api/queues/%2F"), "name", "durable", "messages", "consumers");
    }

    // ---------- Exchanges ----------

    public Map<String, Object> crearExchange(ExchangeRequest r) {
        validarExchangeNoProtegido(r.name());
        Exchange exchange = switch (r.type()) {
            case "topic" -> ExchangeBuilder.topicExchange(r.name()).durable(r.durable()).build();
            case "fanout" -> ExchangeBuilder.fanoutExchange(r.name()).durable(r.durable()).build();
            case "headers" -> ExchangeBuilder.headersExchange(r.name()).durable(r.durable()).build();
            default -> ExchangeBuilder.directExchange(r.name()).durable(r.durable()).build();
        };
        admin.declareExchange(exchange);
        log.info("[ADMIN] Exchange creado: {} ({})", r.name(), r.type());
        return Map.of("exchange", r.name(), "type", r.type(), "durable", r.durable());
    }

    public void eliminarExchange(String name) {
        validarExchangeNoProtegido(name);
        admin.deleteExchange(name);
        log.info("[ADMIN] Exchange eliminado: {}", name);
    }

    public List<Map<String, Object>> listarExchanges() {
        return filtrar(obtener("/api/exchanges/%2F"), "name", "type", "durable");
    }

    // ---------- Bindings ----------

    public Map<String, Object> crearBinding(BindingRequest r) {
        if (admin.getQueueProperties(r.queue()) == null) {
            throw new RecursoNoEncontradoException("La cola '" + r.queue() + "' no existe");
        }
        admin.declareBinding(new Binding(r.queue(), Binding.DestinationType.QUEUE,
                r.exchange(), r.routingKey(), null));
        log.info("[ADMIN] Binding creado: {} -> {} ({})", r.exchange(), r.queue(), r.routingKey());
        return Map.of("exchange", r.exchange(), "queue", r.queue(), "routingKey", r.routingKey());
    }

    public void eliminarBinding(BindingRequest r) {
        if (props.protectedQueues().contains(r.queue())) {
            throw new RecursoProtegidoException("La cola '" + r.queue() + "' es gestionada por otro servicio");
        }
        admin.removeBinding(new Binding(r.queue(), Binding.DestinationType.QUEUE,
                r.exchange(), r.routingKey(), null));
        log.info("[ADMIN] Binding eliminado: {} -> {} ({})", r.exchange(), r.queue(), r.routingKey());
    }

    public List<Map<String, Object>> listarBindings() {
        List<Map<String, Object>> todos = obtener("/api/bindings/%2F");
        return filtrar(todos.stream()
                .filter(b -> !"".equals(b.get("source")) && "queue".equals(b.get("destination_type")))
                .toList(), "source", "destination", "routing_key");
    }

    // ---------- Utilidades ----------

    private void validarColaNoProtegida(String name) {
        if (props.protectedQueues().contains(name)) {
            throw new RecursoProtegidoException("La cola '" + name + "' es gestionada por otro servicio");
        }
    }

    private void validarExchangeNoProtegido(String name) {
        if (props.protectedExchanges().contains(name)) {
            throw new RecursoProtegidoException("El exchange '" + name + "' es gestionado por otro servicio");
        }
    }

    private List<Map<String, Object>> obtener(String path) {
        return rest.get().uri(URI.create(props.managementUrl() + path)).retrieve().body(LISTA);
    }

    private List<Map<String, Object>> filtrar(List<Map<String, Object>> origen, String... campos) {
        return origen.stream().map(fila -> {
            Map<String, Object> salida = new LinkedHashMap<>();
            for (String c : campos) {
                salida.put(c, fila.get(c));
            }
            return salida;
        }).toList();
    }
}
