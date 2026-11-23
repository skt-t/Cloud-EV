package com.trans.service.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferenciaDlqKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaDlqKafkaConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic-transacciones}.DLT", groupId = "transacciones-dlt-group")
    public void consumirDlq(ConsumerRecord<String, Object> record) {
        log.error("[KAFKA DLT] Mensaje fallido recibido en Dead Letter Topic: Topic={}, Partition={}, Offset={}, Key={}, Value={}",
                record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
}
