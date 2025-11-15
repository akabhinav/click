package com.adtech.clickaggregator.application.usecase;

import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.adtech.clickaggregator.domain.port.in.ProcessClickEventUseCase;
import com.adtech.clickaggregator.domain.port.out.ClickEventRepository;
import com.adtech.clickaggregator.domain.port.out.EventPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of click event processing use case.
 * Handles validation, persistence, and async event publishing.
 */
@Slf4j
@Service
public class ProcessClickEventUseCaseImpl implements ProcessClickEventUseCase {

    private final ClickEventRepository clickEventRepository;
    private final EventPublisher eventPublisher;
    private final Counter clickCounter;
    private final Counter invalidClickCounter;

    public ProcessClickEventUseCaseImpl(
            ClickEventRepository clickEventRepository,
            EventPublisher eventPublisher,
            MeterRegistry meterRegistry) {
        this.clickEventRepository = clickEventRepository;
        this.eventPublisher = eventPublisher;
        this.clickCounter = Counter.builder("clicks.processed")
                .description("Total number of clicks processed")
                .register(meterRegistry);
        this.invalidClickCounter = Counter.builder("clicks.invalid")
                .description("Total number of invalid clicks")
                .register(meterRegistry);
    }

    @Override
    public ClickEvent processClick(ClickEvent event) {
        log.debug("Processing click event asynchronously: {}", event.getEventId());

        // Validate event
        if (!event.isValid()) {
            log.warn("Invalid click event received: {}", event);
            invalidClickCounter.increment();
            throw new IllegalArgumentException("Invalid click event");
        }

        // Publish to message broker for async processing
        eventPublisher.publishClickEvent(event, event.getCampaignId());
        clickCounter.increment();

        log.debug("Click event published for async processing: {}", event.getEventId());
        return event;
    }

    @Override
    @Transactional
    public ClickEvent processClickSync(ClickEvent event) {
        log.debug("Processing click event synchronously: {}", event.getEventId());

        // Validate event
        if (!event.isValid()) {
            log.warn("Invalid click event received: {}", event);
            invalidClickCounter.increment();
            throw new IllegalArgumentException("Invalid click event");
        }

        // Save directly to database
        ClickEvent savedEvent = clickEventRepository.save(event);
        clickCounter.increment();

        log.info("Click event processed synchronously: {}", savedEvent.getEventId());
        return savedEvent;
    }
}
