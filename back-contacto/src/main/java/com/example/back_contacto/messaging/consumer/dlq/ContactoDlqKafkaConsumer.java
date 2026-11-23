package com.example.back_contacto.messaging.consumer.dlq;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ContactoDlqKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ContactoDlqKafkaConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic-usuarios}.DLT", groupId = "contactos-dlt-group")
    public void consumirDlq(ConsumerRecord<String, Object> record) {
        log.error("[KAFKA DLT] Mensaje de contacto fallido recibido en Dead Letter Topic: Topic={}, Partition={}, Offset={}, Key={}, Value={}",
                record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
}
