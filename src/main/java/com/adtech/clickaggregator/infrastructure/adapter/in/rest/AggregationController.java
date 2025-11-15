package com.adtech.clickaggregator.infrastructure.adapter.in.rest;

import com.adtech.clickaggregator.domain.model.AggregationDimension;
import com.adtech.clickaggregator.domain.model.AggregationQuery;
import com.adtech.clickaggregator.domain.model.ClickAggregation;
import com.adtech.clickaggregator.domain.port.in.QueryAggregationUseCase;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto.AggregationResponse;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.mapper.AggregationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for querying aggregated click data.
 * Provides analytics and reporting endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/aggregations")
@Tag(name = "Aggregations", description = "Click data aggregation and analytics API")
public class AggregationController {

    private final QueryAggregationUseCase queryAggregationUseCase;
    private final AggregationMapper aggregationMapper;

    public AggregationController(
            QueryAggregationUseCase queryAggregationUseCase,
            AggregationMapper aggregationMapper) {
        this.queryAggregationUseCase = queryAggregationUseCase;
        this.aggregationMapper = aggregationMapper;
    }

    @GetMapping
    @Operation(
            summary = "Query aggregations",
            description = "Query aggregated click data by dimension and time range"
    )
    public ResponseEntity<List<AggregationResponse>> queryAggregations(
            @Parameter(description = "Aggregation dimension (AD, CAMPAIGN, COUNTRY, DEVICE_TYPE)")
            @RequestParam AggregationDimension dimension,

            @Parameter(description = "Start time (ISO-8601 format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,

            @Parameter(description = "End time (ISO-8601 format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,

            @Parameter(description = "Maximum number of results")
            @RequestParam(required = false, defaultValue = "100") Integer limit,

            @Parameter(description = "Offset for pagination")
            @RequestParam(required = false, defaultValue = "0") Integer offset) {

        log.info("Querying aggregations: dimension={}, startTime={}, endTime={}",
                dimension, startTime, endTime);

        AggregationQuery query = AggregationQuery.builder()
                .dimension(dimension)
                .startTime(startTime)
                .endTime(endTime)
                .limit(limit)
                .offset(offset)
                .build();

        List<ClickAggregation> aggregations = queryAggregationUseCase.queryAggregations(query);
        List<AggregationResponse> responses = aggregationMapper.toResponseList(aggregations);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/top/ads")
    @Operation(
            summary = "Get top performing ads",
            description = "Get top N ads by click count"
    )
    public ResponseEntity<List<AggregationResponse>> getTopAds(
            @Parameter(description = "Number of results")
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Fetching top {} ads", limit);

        List<ClickAggregation> topAds = queryAggregationUseCase.getTopAds(limit);
        List<AggregationResponse> responses = aggregationMapper.toResponseList(topAds);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/top/campaigns")
    @Operation(
            summary = "Get top performing campaigns",
            description = "Get top N campaigns by total cost"
    )
    public ResponseEntity<List<AggregationResponse>> getTopCampaigns(
            @Parameter(description = "Number of results")
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Fetching top {} campaigns", limit);

        List<ClickAggregation> topCampaigns = queryAggregationUseCase.getTopCampaigns(limit);
        List<AggregationResponse> responses = aggregationMapper.toResponseList(topCampaigns);

        return ResponseEntity.ok(responses);
    }
}
