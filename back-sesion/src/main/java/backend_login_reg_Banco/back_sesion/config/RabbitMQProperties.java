package backend_login_reg_Banco.back_sesion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Nombres de exchange, colas y routing keys, leídos de application.properties (prefijo app.rabbitmq). */
@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMQProperties(
        String exchange,
        String dlx,
        String routingUsuario,
        String queueUsuario,
        String dlqUsuario) {
}
