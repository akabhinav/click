package com.adtech.clickaggregator.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClickEvent domain model
 */
class ClickEventTest {

    @Test
    void testCreateClickEvent() {
        ClickEvent event = ClickEvent.create(
                "ad-123",
                "campaign-456",
                "user-789",
                "192.168.1.1",
                "Mozilla/5.0",
                "US",
                "mobile",
                0.25
        );

        assertNotNull(event.getEventId());
        assertEquals("ad-123", event.getAdId());
        assertEquals("campaign-456", event.getCampaignId());
        assertNotNull(event.getTimestamp());
        assertEquals(0.25, event.getCost());
    }

    @Test
    void testIsValid_ValidEvent() {
        ClickEvent event = ClickEvent.builder()
                .adId("ad-123")
                .campaignId("campaign-456")
                .timestamp(Instant.now())
                .cost(0.25)
                .build();

        assertTrue(event.isValid());
    }

    @Test
    void testIsValid_MissingAdId() {
        ClickEvent event = ClickEvent.builder()
                .campaignId("campaign-456")
                .timestamp(Instant.now())
                .cost(0.25)
                .build();

        assertFalse(event.isValid());
    }

    @Test
    void testIsValid_NegativeCost() {
        ClickEvent event = ClickEvent.builder()
                .adId("ad-123")
                .campaignId("campaign-456")
                .timestamp(Instant.now())
                .cost(-0.25)
                .build();

        assertFalse(event.isValid());
    }

    @Test
    void testGetHourBucket() {
        Instant timestamp = Instant.parse("2024-01-15T14:35:20Z");
        ClickEvent event = ClickEvent.builder()
                .adId("ad-123")
                .campaignId("campaign-456")
                .timestamp(timestamp)
                .cost(0.25)
                .build();

        Instant hourBucket = event.getHourBucket();
        assertEquals(Instant.parse("2024-01-15T14:00:00Z"), hourBucket);
    }

    @Test
    void testGetDayBucket() {
        Instant timestamp = Instant.parse("2024-01-15T14:35:20Z");
        ClickEvent event = ClickEvent.builder()
                .adId("ad-123")
                .campaignId("campaign-456")
                .timestamp(timestamp)
                .cost(0.25)
                .build();

        Instant dayBucket = event.getDayBucket();
        assertEquals(Instant.parse("2024-01-15T00:00:00Z"), dayBucket);
    }
}
