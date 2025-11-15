package com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * REST API request DTO for click events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventRequest {

    @NotBlank(message = "Ad ID is required")
    @JsonProperty("ad_id")
    private String adId;

    @NotBlank(message = "Campaign ID is required")
    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("user_agent")
    private String userAgent;

    private String country;

    @JsonProperty("device_type")
    private String deviceType;

    @NotNull(message = "Cost is required")
    @Positive(message = "Cost must be positive")
    private Double cost;
}
