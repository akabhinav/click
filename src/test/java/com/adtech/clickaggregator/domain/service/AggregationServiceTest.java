package com.adtech.clickaggregator.domain.service;

import com.adtech.clickaggregator.domain.model.AggregationDimension;
import com.adtech.clickaggregator.domain.model.ClickAggregation;
import com.adtech.clickaggregator.domain.model.ClickEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AggregationService
 */
class AggregationServiceTest {

    private AggregationService aggregationService;

    @BeforeEach
    void setUp() {
        aggregationService = new AggregationService();
    }

    @Test
    void testCreateAggregations() {
        ClickEvent event = ClickEvent.builder()
                .eventId("event-1")
                .adId("ad-123")
                .campaignId("campaign-456")
                .country("US")
                .deviceType("mobile")
                .timestamp(Instant.now())
                .cost(0.50)
                .build();

        List<ClickAggregation> aggregations = aggregationService.createAggregations(event);

        assertEquals(4, aggregations.size());

        // Verify all dimensions are present
        assertTrue(aggregations.stream().anyMatch(a -> a.getDimension() == AggregationDimension.AD));
        assertTrue(aggregations.stream().anyMatch(a -> a.getDimension() == AggregationDimension.CAMPAIGN));
        assertTrue(aggregations.stream().anyMatch(a -> a.getDimension() == AggregationDimension.COUNTRY));
        assertTrue(aggregations.stream().anyMatch(a -> a.getDimension() == AggregationDimension.DEVICE_TYPE));

        // Verify aggregation values
        ClickAggregation adAgg = aggregations.stream()
                .filter(a -> a.getDimension() == AggregationDimension.AD)
                .findFirst()
                .orElseThrow();

        assertEquals("ad-123", adAgg.getDimensionValue());
        assertEquals(1L, adAgg.getClickCount());
        assertEquals(0.50, adAgg.getTotalCost());
    }

    @Test
    void testMergeAggregations() {
        Instant timeBucket = Instant.now();

        ClickAggregation agg1 = ClickAggregation.builder()
                .aggregationId("agg-1")
                .dimension(AggregationDimension.AD)
                .dimensionValue("ad-123")
                .timeBucket(timeBucket)
                .clickCount(5L)
                .totalCost(2.50)
                .uniqueUsers(5L)
                .lastUpdated(Instant.now())
                .build();

        ClickAggregation agg2 = ClickAggregation.builder()
                .aggregationId("agg-2")
                .dimension(AggregationDimension.AD)
                .dimensionValue("ad-123")
                .timeBucket(timeBucket)
                .clickCount(3L)
                .totalCost(1.50)
                .uniqueUsers(3L)
                .lastUpdated(Instant.now())
                .build();

        ClickAggregation merged = aggregationService.mergeAggregations(List.of(agg1, agg2));

        assertEquals(8L, merged.getClickCount());
        assertEquals(4.00, merged.getTotalCost(), 0.001);
        assertEquals(5L, merged.getUniqueUsers()); // Max of the two
    }

    @Test
    void testIsValidAggregation() {
        ClickAggregation valid = ClickAggregation.builder()
                .dimension(AggregationDimension.AD)
                .dimensionValue("ad-123")
                .timeBucket(Instant.now())
                .clickCount(10L)
                .totalCost(5.0)
                .build();

        assertTrue(aggregationService.isValidAggregation(valid));
    }

    @Test
    void testIsValidAggregation_NegativeCost() {
        ClickAggregation invalid = ClickAggregation.builder()
                .dimension(AggregationDimension.AD)
                .dimensionValue("ad-123")
                .timeBucket(Instant.now())
                .clickCount(10L)
                .totalCost(-5.0)
                .build();

        assertFalse(aggregationService.isValidAggregation(invalid));
    }
}
