package com.adtech.clickaggregator.infrastructure.adapter.out.persistence.entity;

import com.adtech.clickaggregator.domain.model.AggregationDimension;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for click aggregations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "click_aggregations",
        indexes = {
                @Index(name = "idx_dimension_value_time", columnList = "dimension,dimension_value,time_bucket"),
                @Index(name = "idx_dimension_time", columnList = "dimension,time_bucket"),
                @Index(name = "idx_click_count", columnList = "click_count DESC"),
                @Index(name = "idx_total_cost", columnList = "total_cost DESC")
        }
)
public class ClickAggregationEntity {

    @Id
    @Column(name = "aggregation_id", nullable = false, length = 200)
    private String aggregationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dimension", nullable = false, length = 50)
    private AggregationDimension dimension;

    @Column(name = "dimension_value", nullable = false, length = 100)
    private String dimensionValue;

    @Column(name = "time_bucket", nullable = false)
    private Instant timeBucket;

    @Column(name = "click_count", nullable = false)
    private Long clickCount;

    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    @Column(name = "unique_users", nullable = false)
    private Long uniqueUsers;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (lastUpdated == null) {
            lastUpdated = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = Instant.now();
    }
}
