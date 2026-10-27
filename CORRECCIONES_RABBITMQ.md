# Tarea: aplicar correcciones y crear pruebas unitarias de RabbitMQ

Proyecto Spring Boot 4.1.1, Java 21, paquete `com.trans.service`, rama `backend-trans`.
Usa `./mvnw` (no `mvn`).

## Reglas
- NO toques la carpeta `security/`.
- NO ejecutes `git commit`, `git push` ni `git checkout`.
- NO escribas contraseñas reales: usa siempre variables de entorno.
- NO borres nada de `pom.xml` salvo lo que se indica abajo.
- Si el contenido de un archivo ya coincide con lo pedido, no lo cambies y dilo.
- Lee cada archivo antes de editarlo.

## Paso 1: application.properties
Reemplaza TODO el contenido de `src/main/resources/application.properties` por exactamente esto:

```properties
server.port=8082
spring.application.name=service

spring.datasource.url=jdbc:mysql://banco-cloud-db.cmggybjdfvmj.us-east-1.rds.amazonaws.com:3306/banco_cloud_db
spring.datasource.username=admin
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update

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

## Paso 2: pom.xml
1. Busca con `grep -n -i h2 pom.xml`. Si existe una dependencia `com.h2database:h2`, elimínala. Si no existe, no hagas nada y dilo.
2. Confirma que existen `spring-boot-starter-amqp` y `spring-boot-starter-test` (scope test), sin `<version>`. Si falta alguna, agrégala dentro de `<dependencies>`.

## Paso 3: converter
En `src/main/java/com/trans/service/config/RabbitMQConfig.java` confirma que se usa `JacksonJsonMessageConverter` (import y `new`). Si aparece `Jackson2JsonMessageConverter`, reemplázalo.

## Paso 4: borrar la prueba que falla
Borra `src/test/java/com/trans/service/ServiceApplicationTests.java` si existe. Levanta todo el contexto de Spring y necesita la base de datos y RabbitMQ, por lo que falla sin ellos.

## Paso 5: pruebas unitarias con Mockito (sin @SpringBootTest)
Crea la carpeta `src/test/java/com/trans/service/messaging/` y estos dos archivos. Si ya existen, compara y solo corrige lo que difiera.

### TransferenciaEventPublisherTest.java
Antes de escribirlo, lee `entities/Transaction.java` por si hay que ajustar algo. Las pruebas usan un mock de `Transaction`.

```java
package com.trans.service.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.trans.service.config.RabbitMQProperties;
import com.trans.service.entities.Transaction;

@ExtendWith(MockitoExtension.class)
class TransferenciaEventPublisherTest {

    @Mock
    private RabbitTemplate rabbit;

    private final RabbitMQProperties props = new RabbitMQProperties(
            "banco.events", "banco.dlx", "transferencia.realizada",
            "cola.notificaciones", "cola.comprobante",
            "dlq.notificaciones", "dlq.comprobante");

    @Test
    void publicaElEventoEnElExchangeCorrecto() {
        Transaction t = mock(Transaction.class);
        TransferenciaEventPublisher publisher = new TransferenciaEventPublisher(rabbit, props);

        publisher.publicar(t);

        verify(rabbit).convertAndSend(
                eq("banco.events"), eq("transferencia.realizada"), any(Object.class));
    }

    @Test
    void noPropagaLaExcepcionSiRabbitFalla() {
        Transaction t = mock(Transaction.class);
        doThrow(new AmqpException("RabbitMQ caído"))
                .when(rabbit).convertAndSend(any(String.class), any(String.class), any(Object.class));
        TransferenciaEventPublisher publisher = new TransferenciaEventPublisher(rabbit, props);

        assertDoesNotThrow(() -> publisher.publicar(t));
    }
}
```

### TransferenciaConsumerTest.java
```java
package com.trans.service.messaging;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.rabbitmq.client.Channel;
import com.trans.service.dto.TransferenciaRealizadaEvent;

@ExtendWith(MockitoExtension.class)
class TransferenciaConsumerTest {

    @Mock
    private Channel channel;

    private final TransferenciaConsumer consumer = new TransferenciaConsumer();

    private Message mensaje() {
        return new Message(new byte[0], new MessageProperties());
    }

    private TransferenciaRealizadaEvent eventoValido() {
        return new TransferenciaRealizadaEvent(1L, BigDecimal.TEN, LocalDateTime.now());
    }

    @Test
    void eventoValidoHaceAck() throws Exception {
        consumer.notificar(eventoValido(), mensaje(), channel, 5L);

        verify(channel).basicAck(5L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void eventoNuloVaALaDlq() throws Exception {
        consumer.notificar(null, mensaje(), channel, 5L);

        verify(channel).basicNack(5L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void eventoSinIdVaALaDlq() throws Exception {
        TransferenciaRealizadaEvent sinId =
                new TransferenciaRealizadaEvent(null, BigDecimal.TEN, LocalDateTime.now());

        consumer.comprobante(sinId, mensaje(), channel, 7L);

        verify(channel).basicNack(7L, false, false);
    }

    @Test
    void falloTemporalReintentaUnaVez() throws Exception {
        doThrow(new IOException("fallo")).when(channel).basicAck(5L, false);

        consumer.notificar(eventoValido(), mensaje(), channel, 5L);

        verify(channel).basicNack(5L, false, true);
    }
}
```

Si algún método o campo de los tests no coincide con las clases reales (nombres, firmas), ajusta el test, no el código de producción.

## Paso 6: verificar
1. Ejecuta `./mvnw clean compile` y corrige errores.
2. Ejecuta `./mvnw clean test` y corrige errores en los tests.
3. Ejecuta `git status` y `git diff --stat`.
4. Confirma con `git diff HEAD -- src/main/java/com/trans/service/security` que NO hay cambios en `security/`.

## Formato de la respuesta
Entrega:
1. Lista de archivos creados, modificados y borrados.
2. Resultado de `compile` y `test` (cuántas pruebas pasaron).
3. Cualquier cosa que no pudiste aplicar y por qué.

No hagas commit ni push.
