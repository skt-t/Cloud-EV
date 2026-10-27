package com.example.back_contacto.config;

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

/**
 * Declara los beans de Exchange, Queue y Binding de cada caso de uso.
 * Spring Boot (RabbitAdmin) los crea en el broker al conectarse.
 *
 * Topología:
 *   contactos.exchange (topic) --contacto.creado-->   contactos.creado.queue   --(rechazo)--> contactos.dlx --> contactos.creado.dlq
 *   contactos.exchange (topic) --contacto.validado--> contactos.validado.queue --(rechazo)--> contactos.dlx --> contactos.validado.dlq
 */
@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    private final RabbitMQProperties props;

    public RabbitMQConfig(RabbitMQProperties props) {
        this.props = props;
    }

    // ---------------------------------------------------------------- Exchanges

    @Bean
    public TopicExchange contactosExchange() {
        return new TopicExchange(props.getExchange(), true, false);
    }

    @Bean
    public DirectExchange contactosDeadLetterExchange() {
        return new DirectExchange(props.getDlx(), true, false);
    }

    // ------------------------------------------- Caso de uso 1: contacto creado

    @Bean
    public Queue contactoCreadoQueue() {
        return QueueBuilder.durable(props.getContactoCreado().getQueue())
                .deadLetterExchange(props.getDlx())
                .deadLetterRoutingKey(props.getContactoCreado().getDlqRoutingKey())
                .build();
    }

    @Bean
    public Queue contactoCreadoDlq() {
        return QueueBuilder.durable(props.getContactoCreado().getDlq()).build();
    }

    @Bean
    public Binding contactoCreadoBinding() {
        return BindingBuilder.bind(contactoCreadoQueue())
                .to(contactosExchange())
                .with(props.getContactoCreado().getRoutingKey());
    }

    @Bean
    public Binding contactoCreadoDlqBinding() {
        return BindingBuilder.bind(contactoCreadoDlq())
                .to(contactosDeadLetterExchange())
                .with(props.getContactoCreado().getDlqRoutingKey());
    }

    // ----------------------------------------- Caso de uso 2: contacto validado

    @Bean
    public Queue contactoValidadoQueue() {
        return QueueBuilder.durable(props.getContactoValidado().getQueue())
                .deadLetterExchange(props.getDlx())
                .deadLetterRoutingKey(props.getContactoValidado().getDlqRoutingKey())
                .build();
    }

    @Bean
    public Queue contactoValidadoDlq() {
        return QueueBuilder.durable(props.getContactoValidado().getDlq()).build();
    }

    @Bean
    public Binding contactoValidadoBinding() {
        return BindingBuilder.bind(contactoValidadoQueue())
                .to(contactosExchange())
                .with(props.getContactoValidado().getRoutingKey());
    }

    @Bean
    public Binding contactoValidadoDlqBinding() {
        return BindingBuilder.bind(contactoValidadoDlq())
                .to(contactosDeadLetterExchange())
                .with(props.getContactoValidado().getDlqRoutingKey());
    }

    // ---------------------------------------------------------------- Conversor

    /** Serializa/deserializa los eventos como JSON. Spring Boot lo usa en RabbitTemplate y en los listeners. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
