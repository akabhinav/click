package com.adtech.clickaggregator.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Core domain model representing an ad click event.
 * Immutable value object that captures essential click information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent {

    private String eventId;
    private String adId;
    private String campaignId;
    private String userId;
    private String ipAddress;
    private String userAgent;
    private String country;
    private String deviceType;
    private Instant timestamp;
    private Double cost;

    /**
     * Factory method to create a new click event with generated ID
     */
    public static ClickEvent create(String adId, String campaignId, String userId,
                                   String ipAddress, String userAgent, String country,
                                   String deviceType, Double cost) {
        return ClickEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .adId(adId)
                .campaignId(campaignId)
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .country(country)
                .deviceType(deviceType)
                .timestamp(Instant.now())
                .cost(cost)
                .build();
    }

    /**
     * Validates if the click event has all required fields
     */
    public boolean isValid() {
        return adId != null && !adId.isBlank()
                && campaignId != null && !campaignId.isBlank()
                && timestamp != null
                && cost != null && cost >= 0;
    }

    /**
     * Gets the time bucket (hour) for aggregation purposes
     */
    public Instant getHourBucket() {
        long epochSecond = timestamp.getEpochSecond();
        long hourBucketSeconds = (epochSecond / 3600) * 3600;
        return Instant.ofEpochSecond(hourBucketSeconds);
    }

    /**
     * Gets the time bucket (day) for aggregation purposes
     */
    public Instant getDayBucket() {
        long epochSecond = timestamp.getEpochSecond();
        long dayBucketSeconds = (epochSecond / 86400) * 86400;
        return Instant.ofEpochSecond(dayBucketSeconds);
    }
}
