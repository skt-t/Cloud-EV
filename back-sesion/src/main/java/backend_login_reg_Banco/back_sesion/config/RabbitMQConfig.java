package backend_login_reg_Banco.back_sesion.config;

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
 * Topología de mensajería de back-sesion, separada de la lógica de negocio.
 * El exchange banco.events (topic) y el banco.dlx (direct) se declaran igual que en
 * back-contacto y back-trans: mismos parámetros, así RabbitMQ no rechaza la declaración.
 */
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
    Queue usuarioQueue() {
        return QueueBuilder.durable(p.queueUsuario())
                .deadLetterExchange(p.dlx())
                .deadLetterRoutingKey(p.dlqUsuario())
                .build();
    }

    @Bean
    Queue usuarioDlq() {
        return QueueBuilder.durable(p.dlqUsuario()).build();
    }

    @Bean
    Binding usuarioBinding() {
        return BindingBuilder.bind(usuarioQueue())
                .to(eventsExchange()).with(p.routingUsuario());
    }

    @Bean
    Binding usuarioDlqBinding() {
        return BindingBuilder.bind(usuarioDlq())
                .to(deadLetterExchange()).with(p.dlqUsuario());
    }

    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
