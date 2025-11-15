package com.adtech.clickaggregator.infrastructure.adapter.out.persistence.repository;

import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.entity.ClickEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for click events
 */
@Repository
public interface JpaClickEventRepository extends JpaRepository<ClickEventEntity, String> {

    List<ClickEventEntity> findByTimestampBetween(Instant startTime, Instant endTime);

    List<ClickEventEntity> findByCampaignIdAndTimestampBetween(
            String campaignId, Instant startTime, Instant endTime);

    List<ClickEventEntity> findByAdIdAndTimestampBetween(
            String adId, Instant startTime, Instant endTime);

    @Modifying
    @Query("DELETE FROM ClickEventEntity e WHERE e.timestamp < :cutoffTime")
    int deleteOlderThan(@Param("cutoffTime") Instant cutoffTime);
}
