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