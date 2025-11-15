package com.adtech.clickaggregator.domain.port.out;

import com.adtech.clickaggregator.domain.model.ClickEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for click event persistence
 */
public interface ClickEventRepository {

    /**
     * Save a click event
     *
     * @param event the event to save
     * @return the saved event
     */
    ClickEvent save(ClickEvent event);

    /**
     * Save multiple click events in batch
     *
     * @param events list of events to save
     * @return list of saved events
     */
    List<ClickEvent> saveAll(List<ClickEvent> events);

    /**
     * Find a click event by ID
     *
     * @param eventId the event ID
     * @return the event if found
     */
    Optional<ClickEvent> findById(String eventId);

    /**
     * Find events within a time range
     *
     * @param startTime start of the range
     * @param endTime end of the range
     * @return list of events
     */
    List<ClickEvent> findByTimeRange(Instant startTime, Instant endTime);

    /**
     * Count total events
     *
     * @return total count
     */
    long count();

    /**
     * Delete events older than specified time
     *
     * @param cutoffTime the cutoff time
     * @return number of deleted events
     */
    long deleteOlderThan(Instant cutoffTime);
}
