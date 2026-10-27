# Tarea: revisar la implementación de RabbitMQ en transacciones

Proyecto Spring Boot 4.1.1, Java 21, paquete `com.trans.service`, rama `backend-trans`.

## Reglas
- Esta tarea es **solo de revisión**. NO modifiques, crees ni borres ningún archivo.
- NO ejecutes `git commit`, `git push` ni `git checkout`.
- Puedes ejecutar `./mvnw clean compile` y `./mvnw test` (usa `./mvnw`, no `mvn`).
- Lee cada archivo completo antes de opinar. No supongas contenido.
- Si algo está bien, di "OK". Si algo está mal, indica archivo, línea y la corrección sugerida.
- No pegues contraseñas ni claves reales en tu respuesta. Si encuentras una, solo indica archivo y línea.

## Archivos a leer
- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/java/com/trans/service/config/RabbitMQProperties.java`
- `src/main/java/com/trans/service/config/RabbitMQConfig.java`
- `src/main/java/com/trans/service/dto/TransferenciaRealizadaEvent.java`
- `src/main/java/com/trans/service/messaging/TransferenciaEventPublisher.java`
- `src/main/java/com/trans/service/messaging/TransferenciaConsumer.java`
- `src/main/java/com/trans/service/messaging/DeadLetterConsumer.java`
- `src/main/java/com/trans/service/services/TransactionService.java`
- `src/main/java/com/trans/service/entities/Transaction.java`
- `src/test/java/` (todo lo que exista)

## Checklist de revisión

### 1. pom.xml
- Existen `spring-boot-starter-amqp` y `spring-boot-starter-test` (scope test).
- Ninguna dependencia de Spring tiene `<version>` propia.
- No hay dependencia de H2.
- Existe `mysql-connector-j`.

### 2. application.properties
- Existen `server.port=8082`, `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`.
- Las contraseñas usan variables de entorno (`${DB_PASSWORD}`, `${RABBITMQ_PASSWORD}`), nunca valores escritos.
- Están todas las propiedades `spring.rabbitmq.*` con `acknowledge-mode=manual` y `default-requeue-rejected=false`.
- Están todas las propiedades `app.rabbitmq.*`: exchange, dlx, routing-transferencia, queue-notificaciones, queue-comprobante, dlq-notificaciones, dlq-comprobante, dlq-consumer-enabled.
- No hay propiedades `spring.h2.*`.

### 3. RabbitMQProperties y RabbitMQConfig
- Los campos del record coinciden con las claves de `app.rabbitmq.*` (camelCase vs kebab-case).
- Cada cola principal tiene `x-dead-letter-exchange` y `x-dead-letter-routing-key` que apuntan a su DLQ.
- Cada DLQ tiene un Binding al exchange `dlx` con su propio nombre como routing key.
- Cada cola principal tiene un Binding al exchange `banco.events` con la routing key `transferencia.realizada`.
- No hay nombres de colas o exchanges escritos directamente en el código (todo sale de las properties).
- El converter es `JacksonJsonMessageConverter`, no `Jackson2JsonMessageConverter`.

### 4. TransferenciaEventPublisher
- Envuelve el envío en `try/catch` y NO propaga la excepción si RabbitMQ falla.
- Usa `exchange` y `routingTransferencia` desde las properties.
- Los getters usados (`getId`, `getMonto`, etc.) existen realmente en `Transaction.java`.
- Los tipos coinciden con el record `TransferenciaRealizadaEvent`.

### 5. TransferenciaConsumer (ACK manual)
- Cada `@RabbitListener` recibe `Channel` y el `DELIVERY_TAG`.
- Evento válido: hace `basicAck`.
- Evento `null` o con `idTransaccion` nulo: hace `basicNack(tag, false, false)` y va a la DLQ.
- Excepción: reintenta una vez (`requeue=true` solo si `!isRedelivered()`) y luego va a la DLQ.
- Nunca queda una ruta de ejecución sin ack ni nack.
- Los dos listeners usan `${app.rabbitmq...}` y no nombres fijos.

### 6. DeadLetterConsumer
- Solo se crea con `@ConditionalOnProperty(name = "app.rabbitmq.dlq-consumer-enabled", havingValue = "true")`.
- Escucha las dos DLQ.
- Solo registra en el log, sin lógica extra.

### 7. TransactionService
- Inyecta `TransferenciaEventPublisher`.
- Llama `publicar(...)` DESPUÉS de guardar la transacción en la base de datos.
- Una falla en RabbitMQ no puede revertir ni romper el guardado de la transacción.
- No se perdió ni cambió la lógica original del servicio.

### 8. Seguridad
- La carpeta `security/` no fue modificada para esta tarea (compara con `git diff HEAD -- src/main/java/com/trans/service/security`).
- Ningún archivo nuevo contiene claves ni contraseñas reales.

### 9. Pruebas
- Existen pruebas unitarias con Mockito para el publisher y para el consumer, sin `@SpringBootTest`.
- Ejecuta `./mvnw clean compile` y `./mvnw test` y reporta el resultado.

## Formato de la respuesta
Entrega un resumen con esta estructura:

1. **Resultado de compilación y pruebas** (OK o error con el mensaje).
2. **Tabla** con una fila por archivo: archivo, estado (OK / Problema) y comentario breve.
3. **Lista de problemas** ordenados de más grave a menos grave. Para cada uno: archivo, línea, qué está mal y cómo corregirlo.
4. **Cosas que faltan** (por ejemplo pruebas o archivos).
5. Termina preguntando qué correcciones quiero que apliques. No apliques nada sin que te lo pida.
