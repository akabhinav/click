package com.adtech.clickaggregator.domain.port.in;

import com.adtech.clickaggregator.domain.model.ClickEvent;

/**
 * Inbound port for processing individual click events.
 * This is the primary use case for click ingestion.
 */
public interface ProcessClickEventUseCase {

    /**
     * Process a single click event asynchronously
     *
     * @param event the click event to process
     * @return the processed event with generated ID
     */
    ClickEvent processClick(ClickEvent event);

    /**
     * Process a click event synchronously (for testing/validation)
     *
     * @param event the click event to process
     * @return the processed event
     */
    ClickEvent processClickSync(ClickEvent event);
}
