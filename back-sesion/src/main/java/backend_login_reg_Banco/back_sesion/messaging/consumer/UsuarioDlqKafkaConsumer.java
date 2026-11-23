package backend_login_reg_Banco.back_sesion.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UsuarioDlqKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(UsuarioDlqKafkaConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic-usuarios}.DLT", groupId = "usuarios-dlt-group")
    public void consumirDlq(ConsumerRecord<String, Object> record) {
        log.error("[KAFKA DLT] Mensaje de usuario fallido recibido en Dead Letter Topic: Topic={}, Partition={}, Offset={}, Key={}, Value={}",
                record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
}
