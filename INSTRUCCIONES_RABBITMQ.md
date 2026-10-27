# Tarea: agregar RabbitMQ al microservicio de transacciones

Proyecto Spring Boot 4.1.1, Java 21, paquete base `com.trans.service`.
Haz los cambios directamente en los archivos. No pidas que copie código.

## Reglas
- NO toques la carpeta `security/`.
- NO escribas contraseñas reales en ningún archivo (usa variables de entorno).
- NO pongas `<version>` en dependencias de Spring (las maneja el parent).
- NO borres nada de lo que ya existe en `application.properties` ni en `pom.xml`.
- Usa los nombres reales de getters/campos de `Transaction.java`. Lee la entidad antes de escribir el publisher.
- Si `JacksonJsonMessageConverter` no compila, usa `Jackson2JsonMessageConverter`.

## Paso 1: pom.xml
Agrega dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Paso 2: application.properties
Agrega al final:

```properties
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=5672
spring.rabbitmq.username=${RABBITMQ_USER:banco}
spring.rabbitmq.password=${RABBITMQ_PASSWORD}
spring.rabbitmq.listener.simple.acknowledge-mode=manual
spring.rabbitmq.listener.simple.default-requeue-rejected=false

app.rabbitmq.exchange=banco.events
app.rabbitmq.dlx=banco.dlx
app.rabbitmq.routing-transferencia=transferencia.realizada
app.rabbitmq.queue-notificaciones=notificaciones.transferencia
app.rabbitmq.queue-comprobante=tickets.comprobante
app.rabbitmq.dlq-notificaciones=notificaciones.transferencia.dlq
app.rabbitmq.dlq-comprobante=tickets.comprobante.dlq
app.rabbitmq.dlq-consumer-enabled=false
```

## Paso 3: crear carpetas
Dentro de `src/main/java/com/trans/service/` crea `config` y `messaging`.

## Paso 4: crear archivos

### config/RabbitMQProperties.java
```java
package com.trans.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMQProperties(
        String exchange,
        String dlx,
        String routingTransferencia,
        String queueNotificaciones,
        String queueComprobante,
        String dlqNotificaciones,
        String dlqComprobante) {
}
```

### config/RabbitMQConfig.java
```java
package com.trans.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    private final RabbitMQProperties p;

    public RabbitMQConfig(RabbitMQProperties p) {
        this.p = p;
    }

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(p.exchange(), true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(p.dlx(), true, false);
    }

    @Bean
    Queue notificacionesQueue() {
        return QueueBuilder.durable(p.queueNotificaciones())
                .deadLetterExchange(p.dlx())
                .deadLetterRoutingKey(p.dlqNotificaciones())
                .build();
    }

    @Bean
    Queue comprobanteQueue() {
        return QueueBuilder.durable(p.queueComprobante())
                .deadLetterExchange(p.dlx())
                .deadLetterRoutingKey(p.dlqComprobante())
                .build();
    }

    @Bean
    Queue notificacionesDlq() {
        return QueueBuilder.durable(p.dlqNotificaciones()).build();
    }

    @Bean
    Queue comprobanteDlq() {
        return QueueBuilder.durable(p.dlqComprobante()).build();
    }

    @Bean
    Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(eventsExchange()).with(p.routingTransferencia());
    }

    @Bean
    Binding comprobanteBinding() {
        return BindingBuilder.bind(comprobanteQueue())
                .to(eventsExchange()).with(p.routingTransferencia());
    }

    @Bean
    Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq())
                .to(deadLetterExchange()).with(p.dlqNotificaciones());
    }

    @Bean
    Binding comprobanteDlqBinding() {
        return BindingBuilder.bind(comprobanteDlq())
                .to(deadLetterExchange()).with(p.dlqComprobante());
    }

    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
```

### dto/TransferenciaRealizadaEvent.java
```java
package com.trans.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferenciaRealizadaEvent(
        Long idTransaccion,
        BigDecimal monto,
        LocalDateTime fecha) {
}
```

### messaging/TransferenciaEventPublisher.java
Ajusta `t.getId()` y `t.getMonto()` a los getters reales de `Transaction` (y el tipo del monto si no es BigDecimal).

```java
package com.trans.service.messaging;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.trans.service.config.RabbitMQProperties;
import com.trans.service.dto.TransferenciaRealizadaEvent;
import com.trans.service.entities.Transaction;

@Component
public class TransferenciaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaEventPublisher.class);

    private final RabbitTemplate rabbit;
    private final RabbitMQProperties p;

    public TransferenciaEventPublisher(RabbitTemplate rabbit, RabbitMQProperties p) {
        this.rabbit = rabbit;
        this.p = p;
    }

    public void publicar(Transaction t) {
        try {
            var evento = new TransferenciaRealizadaEvent(
                    t.getId(), t.getMonto(), LocalDateTime.now());
            rabbit.convertAndSend(p.exchange(), p.routingTransferencia(), evento);
            log.info("Evento publicado: transferencia {}", t.getId());
        } catch (Exception e) {
            log.error("No se pudo publicar la transferencia {}: {}", t.getId(), e.getMessage());
        }
    }
}
```

### messaging/TransferenciaConsumer.java
```java
package com.trans.service.messaging;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.trans.service.dto.TransferenciaRealizadaEvent;

@Component
public class TransferenciaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue-notificaciones}")
    public void notificar(TransferenciaRealizadaEvent e, Message m, Channel ch,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesar(e, m, ch, tag, "notificación");
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-comprobante}")
    public void comprobante(TransferenciaRealizadaEvent e, Message m, Channel ch,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesar(e, m, ch, tag, "comprobante");
    }

    private void procesar(TransferenciaRealizadaEvent e, Message m, Channel ch,
            long tag, String tipo) throws IOException {
        try {
            if (e == null || e.idTransaccion() == null) {
                log.warn("Mensaje inválido ({}), enviado a DLQ", tipo);
                ch.basicNack(tag, false, false);
                return;
            }
            log.info("Generando {} de la transferencia {}", tipo, e.idTransaccion());
            ch.basicAck(tag, false);
        } catch (Exception ex) {
            boolean reintento = !m.getMessageProperties().isRedelivered();
            log.error("Fallo en {} ({}), reintento={}", tipo, ex.getMessage(), reintento);
            ch.basicNack(tag, false, reintento);
        }
    }
}
```

### messaging/DeadLetterConsumer.java
```java
package com.trans.service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.rabbitmq.dlq-consumer-enabled", havingValue = "true")
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @RabbitListener(queues = {
            "${app.rabbitmq.dlq-notificaciones}",
            "${app.rabbitmq.dlq-comprobante}" })
    public void registrar(Message m) {
        log.error("DLQ: {}", new String(m.getBody()));
    }
}
```

## Paso 5: conectar con TransactionService
En `services/TransactionService.java`:
1. Inyecta `TransferenciaEventPublisher` (constructor, o campo `private final` si usa `@RequiredArgsConstructor`).
2. Justo después de guardar la transacción:
```java
Transaction saved = repository.save(transaction);
publisher.publicar(saved);
```
Usa los nombres reales del repositorio y variables del servicio. Conserva el resto de la lógica y el valor de retorno.

## Paso 6: pruebas unitarias (Mockito, sin @SpringBootTest)
En `src/test/java/com/trans/service/messaging/`:
- `TransferenciaEventPublisherTest`: mockear `RabbitTemplate` para que lance excepción y verificar que `publicar` NO la propaga.
- `TransferenciaConsumerTest`: mockear `Channel`. Con evento válido verificar `basicAck`. Con evento `null` verificar `basicNack(tag, false, false)`.

## Paso 7: verificar
1. Ejecuta `mvn clean compile` y corrige errores.
2. Ejecuta `mvn test` y corrige errores.
3. Muestra un resumen de archivos creados y modificados.
4. NO ejecutes `git commit` ni `git push`.
