package br.com.itau.transaction_service.infra.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaProducerHelper {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerHelper.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerHelper(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String key, Object payload, String eventType) {
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, payload);

            addHeader(record, MessagingConstants.HEADER_EVENT_TYPE, eventType);

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event {} to topic {}. Key: {}, Error: {}",
                            eventType, topic, key, ex.getMessage(), ex);
                } else {
                    log.debug("Event {} published successfully to topic {}. Offset: {}",
                            eventType, topic, result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("JSON serialization error for event {}", eventType, e);
            throw new RuntimeException("Failed to serialize Kafka event", e);
        }
    }

    private void addHeader(ProducerRecord<String, Object> record, String headerKey, String value) {
        record.headers().add(new RecordHeader(headerKey, value.getBytes(StandardCharsets.UTF_8)));
    }

}
