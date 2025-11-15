package com.adtech.clickaggregator.infrastructure.adapter.out.messaging;

import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.adtech.clickaggregator.domain.port.out.EventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka implementation of EventPublisher port.
 * Publishes click events to Kafka for async processing.
 */
@Slf4j
@Component
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${kafka.topics.click-events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    @Override
    public void publishClickEvent(ClickEvent event) {
        publishClickEvent(event, event.getEventId());
    }

    @Override
    public void publishClickEvent(ClickEvent event, String partitionKey) {
        try {
            String message = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topicName, partitionKey, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Published event {} to partition {} at offset {}",
                            event.getEventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event {}: {}",
                            event.getEventId(), ex.getMessage(), ex);
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Error serializing click event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
