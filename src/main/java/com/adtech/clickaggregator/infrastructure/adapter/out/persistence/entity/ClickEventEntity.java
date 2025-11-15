package com.adtech.clickaggregator.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for click events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "click_events",
        indexes = {
                @Index(name = "idx_campaign_timestamp", columnList = "campaign_id,timestamp"),
                @Index(name = "idx_ad_timestamp", columnList = "ad_id,timestamp"),
                @Index(name = "idx_timestamp", columnList = "timestamp")
        }
)
public class ClickEventEntity {

    @Id
    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "ad_id", nullable = false, length = 100)
    private String adId;

    @Column(name = "campaign_id", nullable = false, length = 100)
    private String campaignId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "country", length = 10)
    private String country;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "cost", nullable = false)
    private Double cost;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
