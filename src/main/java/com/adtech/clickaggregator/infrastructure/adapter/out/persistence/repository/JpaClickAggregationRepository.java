package com.adtech.clickaggregator.infrastructure.adapter.out.persistence.repository;

import com.adtech.clickaggregator.domain.model.AggregationDimension;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.entity.ClickAggregationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for click aggregations
 */
@Repository
public interface JpaClickAggregationRepository extends JpaRepository<ClickAggregationEntity, String> {

    List<ClickAggregationEntity> findByDimensionAndTimeBucketBetween(
            AggregationDimension dimension, Instant startTime, Instant endTime);

    List<ClickAggregationEntity> findByDimensionAndDimensionValueInAndTimeBucketBetween(
            AggregationDimension dimension, List<String> dimensionValues,
            Instant startTime, Instant endTime);

    @Query("SELECT a FROM ClickAggregationEntity a WHERE a.dimension = :dimension " +
            "ORDER BY a.clickCount DESC")
    List<ClickAggregationEntity> findTopByDimensionOrderByClickCount(
            @Param("dimension") AggregationDimension dimension, Pageable pageable);

    @Query("SELECT a FROM ClickAggregationEntity a WHERE a.dimension = :dimension " +
            "ORDER BY a.totalCost DESC")
    List<ClickAggregationEntity> findTopByDimensionOrderByTotalCost(
            @Param("dimension") AggregationDimension dimension, Pageable pageable);

    @Modifying
    @Query("DELETE FROM ClickAggregationEntity a WHERE a.timeBucket < :cutoffTime")
    int deleteOlderThan(@Param("cutoffTime") Instant cutoffTime);
}
