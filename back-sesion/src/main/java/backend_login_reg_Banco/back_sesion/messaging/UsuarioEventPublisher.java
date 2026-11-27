package backend_login_reg_Banco.back_sesion.messaging;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import backend_login_reg_Banco.back_sesion.config.RabbitMQProperties;
import backend_login_reg_Banco.back_sesion.dto.UsuarioRegistradoEvent;
import backend_login_reg_Banco.back_sesion.model.Usuario;

/** Productor: publica usuario.registrado después de guardar. Si RabbitMQ cae, registra el error y no rompe el registro. */
@Component
public class UsuarioEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UsuarioEventPublisher.class);

    private final RabbitTemplate rabbit;
    private final RabbitMQProperties p;
    private final UsuarioKafkaProducer kafkaProducer;

    public UsuarioEventPublisher(RabbitTemplate rabbit, RabbitMQProperties p, UsuarioKafkaProducer kafkaProducer) {
        this.rabbit = rabbit;
        this.p = p;
        this.kafkaProducer = kafkaProducer;
    }

    public void publicar(Usuario u) {
        try {
            var evento = new UsuarioRegistradoEvent(
                    u.getIdUsuario(), u.getNombre(), u.getGmail(), LocalDateTime.now());
            
            // 1. RabbitMQ (Flujo existente intacto)
            rabbit.convertAndSend(p.exchange(), p.routingUsuario(), evento);
            log.info("Evento publicado en RabbitMQ: usuario {} registrado", u.getIdUsuario());

            // 2. Kafka (En paralelo, protegido por try/catch)
            try {
                kafkaProducer.publicar(evento);
            } catch (Exception e) {
                log.error("[KAFKA ERROR] No se pudo publicar en Kafka para usuario {}: {}. RabbitMQ sigue funcionando.",
                        u.getIdUsuario(), e.getMessage());
            }

        } catch (Exception e) {
            log.error("No se pudo publicar el registro del usuario {}: {}", u.getIdUsuario(), e.getMessage());
        }
    }
}
