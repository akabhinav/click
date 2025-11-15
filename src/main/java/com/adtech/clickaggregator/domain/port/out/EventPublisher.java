package com.adtech.clickaggregator.domain.port.out;

import com.adtech.clickaggregator.domain.model.ClickEvent;

/**
 * Outbound port for publishing events to message broker
 */
public interface EventPublisher {

    /**
     * Publish a click event for async processing
     *
     * @param event the event to publish
     */
    void publishClickEvent(ClickEvent event);

    /**
     * Publish a click event to a specific topic/partition
     *
     * @param event the event to publish
     * @param partitionKey key for partitioning
     */
    void publishClickEvent(ClickEvent event, String partitionKey);
}
