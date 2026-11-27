package backend_login_reg_Banco.back_sesion.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import backend_login_reg_Banco.back_sesion.config.KafkaTopicsProperties;
import backend_login_reg_Banco.back_sesion.dto.UsuarioRegistradoEvent;

@Component
public class UsuarioKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(UsuarioKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties props;

    public UsuarioKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicsProperties props) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
    }

    public void publicar(UsuarioRegistradoEvent evento) {
        String topic = props.topicUsuarios();
        String key = String.valueOf(evento.idUsuario());
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, evento);
            record.headers().add("event-type", "usuario.registrado".getBytes());

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA PRODUCER] Evento de usuario {} enviado exitosamente a topic {} [partition: {}, offset: {}]",
                            evento.gmail(), topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA PRODUCER] Error enviando evento de usuario {} a topic {}: {}",
                            evento.gmail(), topic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("[KAFKA PRODUCER] Excepción al enviar usuario {} a Kafka: {}", evento.gmail(), e.getMessage());
        }
    }
}
