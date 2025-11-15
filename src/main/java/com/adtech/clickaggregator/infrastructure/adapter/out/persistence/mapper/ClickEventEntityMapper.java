package com.adtech.clickaggregator.infrastructure.adapter.out.persistence.mapper;

import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.entity.ClickEventEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between ClickEvent domain model and JPA entity
 */
@Component
public class ClickEventEntityMapper {

    public ClickEventEntity toEntity(ClickEvent domain) {
        return ClickEventEntity.builder()
                .eventId(domain.getEventId())
                .adId(domain.getAdId())
                .campaignId(domain.getCampaignId())
                .userId(domain.getUserId())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .country(domain.getCountry())
                .deviceType(domain.getDeviceType())
                .timestamp(domain.getTimestamp())
                .cost(domain.getCost())
                .build();
    }

    public ClickEvent toDomain(ClickEventEntity entity) {
        return ClickEvent.builder()
                .eventId(entity.getEventId())
                .adId(entity.getAdId())
                .campaignId(entity.getCampaignId())
                .userId(entity.getUserId())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .country(entity.getCountry())
                .deviceType(entity.getDeviceType())
                .timestamp(entity.getTimestamp())
                .cost(entity.getCost())
                .build();
    }

    public List<ClickEvent> toDomainList(List<ClickEventEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<ClickEventEntity> toEntityList(List<ClickEvent> domains) {
        return domains.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
