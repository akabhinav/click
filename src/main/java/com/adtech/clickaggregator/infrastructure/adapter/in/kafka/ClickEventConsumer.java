package com.adtech.clickaggregator.infrastructure.adapter.in.kafka;

import com.adtech.clickaggregator.application.service.AggregationProcessor;
import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for processing click events asynchronously.
 * Consumes events from Kafka topics and triggers aggregation processing.
 */
@Slf4j
@Component
public class ClickEventConsumer {

    private final AggregationProcessor aggregationProcessor;
    private final ObjectMapper objectMapper;

    public ClickEventConsumer(
            AggregationProcessor aggregationProcessor,
            ObjectMapper objectMapper) {
        this.aggregationProcessor = aggregationProcessor;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${kafka.topics.click-events}",
            groupId = "${kafka.consumer.group-id}",
            concurrency = "${kafka.consumer.concurrency:3}"
    )
    public void consumeClickEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.debug("Received message from partition {} at offset {}", partition, offset);

        try {
            ClickEvent event = objectMapper.readValue(message, ClickEvent.class);
            log.debug("Processing click event: {}", event.getEventId());

            aggregationProcessor.processClickEvent(event);

            // Manually acknowledge after successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            log.debug("Successfully processed click event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Error processing message from partition {} at offset {}: {}",
                    partition, offset, e.getMessage(), e);
            // In production, implement DLQ (Dead Letter Queue) or retry logic
        }
    }
}
