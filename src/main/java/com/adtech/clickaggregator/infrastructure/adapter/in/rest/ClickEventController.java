package com.adtech.clickaggregator.infrastructure.adapter.in.rest;

import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.adtech.clickaggregator.domain.port.in.ProcessClickEventUseCase;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto.ClickEventRequest;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto.ClickEventResponse;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.mapper.ClickEventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for click event ingestion.
 * High-throughput endpoint designed to handle millions of requests.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/clicks")
@Tag(name = "Click Events", description = "Click event ingestion API")
public class ClickEventController {

    private final ProcessClickEventUseCase processClickEventUseCase;
    private final ClickEventMapper clickEventMapper;

    public ClickEventController(
            ProcessClickEventUseCase processClickEventUseCase,
            ClickEventMapper clickEventMapper) {
        this.processClickEventUseCase = processClickEventUseCase;
        this.clickEventMapper = clickEventMapper;
    }

    @PostMapping
    @Operation(
            summary = "Record a click event",
            description = "Records a single ad click event for processing and aggregation"
    )
    @ApiResponse(responseCode = "202", description = "Click event accepted for processing")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<ClickEventResponse> recordClick(
            @Valid @RequestBody ClickEventRequest request) {

        log.debug("Received click event: adId={}, campaignId={}",
                request.getAdId(), request.getCampaignId());

        try {
            ClickEvent event = clickEventMapper.toDomain(request);
            ClickEvent processedEvent = processClickEventUseCase.processClick(event);
            ClickEventResponse response = clickEventMapper.toResponse(processedEvent);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid click event: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(clickEventMapper.toErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing click event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(clickEventMapper.toErrorResponse("Internal server error"));
        }
    }

    @PostMapping("/batch")
    @Operation(
            summary = "Record multiple click events",
            description = "Records multiple ad click events in a single request for better performance"
    )
    @ApiResponse(responseCode = "202", description = "Click events accepted for processing")
    public ResponseEntity<List<ClickEventResponse>> recordClickBatch(
            @Valid @RequestBody List<ClickEventRequest> requests) {

        log.info("Received batch of {} click events", requests.size());

        try {
            List<ClickEventResponse> responses = requests.stream()
                    .map(request -> {
                        ClickEvent event = clickEventMapper.toDomain(request);
                        ClickEvent processedEvent = processClickEventUseCase.processClick(event);
                        return clickEventMapper.toResponse(processedEvent);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responses);

        } catch (Exception e) {
            log.error("Error processing click event batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the click ingestion service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
