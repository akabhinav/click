package com.adtech.clickaggregator.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model representing aggregated click data.
 * Supports multiple aggregation dimensions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickAggregation {

    private String aggregationId;
    private AggregationDimension dimension;
    private String dimensionValue;
    private Instant timeBucket;
    private Long clickCount;
    private Double totalCost;
    private Long uniqueUsers;
    private Instant lastUpdated;

    /**
     * Merges another aggregation into this one
     */
    public void merge(ClickAggregation other) {
        if (!this.canMergeWith(other)) {
            throw new IllegalArgumentException("Cannot merge incompatible aggregations");
        }

        this.clickCount += other.clickCount;
        this.totalCost += other.totalCost;
        this.uniqueUsers = Math.max(this.uniqueUsers, other.uniqueUsers);
        this.lastUpdated = Instant.now();
    }

    /**
     * Checks if this aggregation can be merged with another
     */
    private boolean canMergeWith(ClickAggregation other) {
        return this.dimension == other.dimension
                && this.dimensionValue.equals(other.dimensionValue)
                && this.timeBucket.equals(other.timeBucket);
    }

    /**
     * Calculates average cost per click
     */
    public double getAverageCostPerClick() {
        return clickCount > 0 ? totalCost / clickCount : 0.0;
    }

    /**
     * Creates an aggregation from a single click event
     */
    public static ClickAggregation fromClickEvent(ClickEvent event, AggregationDimension dimension) {
        String dimensionValue = extractDimensionValue(event, dimension);
        Instant timeBucket = event.getHourBucket();

        return ClickAggregation.builder()
                .aggregationId(generateId(dimension, dimensionValue, timeBucket))
                .dimension(dimension)
                .dimensionValue(dimensionValue)
                .timeBucket(timeBucket)
                .clickCount(1L)
                .totalCost(event.getCost())
                .uniqueUsers(1L)
                .lastUpdated(Instant.now())
                .build();
    }

    private static String extractDimensionValue(ClickEvent event, AggregationDimension dimension) {
        return switch (dimension) {
            case AD -> event.getAdId();
            case CAMPAIGN -> event.getCampaignId();
            case COUNTRY -> event.getCountry();
            case DEVICE_TYPE -> event.getDeviceType();
        };
    }

    private static String generateId(AggregationDimension dimension, String value, Instant timeBucket) {
        return String.format("%s:%s:%d", dimension, value, timeBucket.getEpochSecond());
    }
}
