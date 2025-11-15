package com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * REST API response DTO for click events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventResponse {

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("ad_id")
    private String adId;

    @JsonProperty("campaign_id")
    private String campaignId;

    private Instant timestamp;

    private String status;

    private String message;
}
