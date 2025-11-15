package com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * REST API response DTO for aggregations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationResponse {

    private String dimension;

    @JsonProperty("dimension_value")
    private String dimensionValue;

    @JsonProperty("time_bucket")
    private Instant timeBucket;

    @JsonProperty("click_count")
    private Long clickCount;

    @JsonProperty("total_cost")
    private Double totalCost;

    @JsonProperty("unique_users")
    private Long uniqueUsers;

    @JsonProperty("average_cost_per_click")
    private Double averageCostPerClick;

    @JsonProperty("last_updated")
    private Instant lastUpdated;
}
