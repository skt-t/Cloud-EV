package com.example.back_contacto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nombres de exchanges, colas y routing keys leídos desde application.properties (prefijo app.rabbitmq).
 * Es la única fuente de nombres: ni la lógica de negocio ni los consumidores los escriben a mano.
 */
@Data
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQProperties {

    private String exchange;
    private String dlx;
    private Ruta contactoCreado = new Ruta();
    private Ruta contactoValidado = new Ruta();

    @Data
    public static class Ruta {
        private String queue;
        private String routingKey;
        private String dlq;
        private String dlqRoutingKey;
    }
}
