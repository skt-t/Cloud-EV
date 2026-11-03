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
