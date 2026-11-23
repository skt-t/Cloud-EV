package backend_login_reg_Banco.back_sesion.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import backend_login_reg_Banco.back_sesion.dto.UsuarioRegistradoEvent;
import backend_login_reg_Banco.back_sesion.service.NotificacionUsuarioService;

@Component
public class UsuarioNotificacionKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(UsuarioNotificacionKafkaConsumer.class);

    private final NotificacionUsuarioService service;

    public UsuarioNotificacionKafkaConsumer(NotificacionUsuarioService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${app.kafka.topic-usuarios}", groupId = "notificaciones-usuario-group")
    public void consumir(@Payload UsuarioRegistradoEvent evento, ConsumerRecord<String, Object> record) {
        log.info("[KAFKA CONSUMER - USUARIOS] Topic: {}, Partition: {}, Offset: {}, Key: {}",
                record.topic(), record.partition(), record.offset(), record.key());

        if (evento == null || evento.gmail() == null || evento.idUsuario() == null) {
            log.error("[KAFKA VALIDATION ERROR] Payload de usuario inválido o nulo: {}", evento);
            throw new IllegalArgumentException("Payload de usuario inválido o nulo");
        }

        service.notificarBienvenida(evento);
    }
}
