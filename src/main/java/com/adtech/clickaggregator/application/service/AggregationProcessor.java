package com.adtech.clickaggregator.application.service;

import com.adtech.clickaggregator.domain.model.ClickAggregation;
import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.adtech.clickaggregator.domain.port.out.AggregationRepository;
import com.adtech.clickaggregator.domain.port.out.CacheService;
import com.adtech.clickaggregator.domain.port.out.ClickEventRepository;
import com.adtech.clickaggregator.domain.service.AggregationService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

/**
 * Processes click events and updates aggregations.
 * This is the core aggregation engine.
 */
@Slf4j
@Service
public class AggregationProcessor {

    private static final Duration AGGREGATION_CACHE_TTL = Duration.ofMinutes(10);
    private static final String AGG_CACHE_PREFIX = "agg:current:";

    private final ClickEventRepository clickEventRepository;
    private final AggregationRepository aggregationRepository;
    private final AggregationService aggregationService;
    private final CacheService cacheService;
    private final Counter aggregationCounter;

    public AggregationProcessor(
            ClickEventRepository clickEventRepository,
            AggregationRepository aggregationRepository,
            AggregationService aggregationService,
            CacheService cacheService,
            MeterRegistry meterRegistry) {
        this.clickEventRepository = clickEventRepository;
        this.aggregationRepository = aggregationRepository;
        this.aggregationService = aggregationService;
        this.cacheService = cacheService;
        this.aggregationCounter = Counter.builder("aggregations.processed")
                .description("Total number of aggregations processed")
                .register(meterRegistry);
    }

    /**
     * Process a click event and update all relevant aggregations
     */
    @Transactional
    public void processClickEvent(ClickEvent event) {
        log.debug("Processing click event for aggregation: {}", event.getEventId());

        try {
            // Save the raw click event
            clickEventRepository.save(event);

            // Create aggregations for all dimensions
            List<ClickAggregation> aggregations = aggregationService.createAggregations(event);

            // Update each aggregation
            for (ClickAggregation aggregation : aggregations) {
                updateAggregation(aggregation);
                aggregationCounter.increment();
            }

            log.info("Successfully processed click event {} with {} aggregations",
                    event.getEventId(), aggregations.size());

        } catch (Exception e) {
            log.error("Error processing click event {}: {}", event.getEventId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update or create an aggregation
     */
    private void updateAggregation(ClickAggregation newAggregation) {
        String aggregationId = newAggregation.getAggregationId();

        // Try to get existing aggregation from cache or database
        ClickAggregation existing = getExistingAggregation(aggregationId);

        if (existing != null) {
            // Merge with existing
            existing.merge(newAggregation);
            aggregationRepository.save(existing);
            cacheService.put(AGG_CACHE_PREFIX + aggregationId, existing, AGGREGATION_CACHE_TTL);
        } else {
            // Save new aggregation
            aggregationRepository.save(newAggregation);
            cacheService.put(AGG_CACHE_PREFIX + aggregationId, newAggregation, AGGREGATION_CACHE_TTL);
        }
    }

    /**
     * Get existing aggregation from cache or database
     */
    private ClickAggregation getExistingAggregation(String aggregationId) {
        // Try cache first
        return cacheService.get(AGG_CACHE_PREFIX + aggregationId, ClickAggregation.class)
                .orElseGet(() -> aggregationRepository.findById(aggregationId).orElse(null));
    }

    /**
     * Process events in batch for better performance
     */
    @Transactional
    public void processBatch(List<ClickEvent> events) {
        log.info("Processing batch of {} events", events.size());

        // Save all events
        clickEventRepository.saveAll(events);

        // Process aggregations
        events.forEach(this::processClickEvent);

        log.info("Batch processing completed for {} events", events.size());
    }
}
