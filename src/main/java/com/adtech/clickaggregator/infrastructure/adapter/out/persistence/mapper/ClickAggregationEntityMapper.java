package com.adtech.clickaggregator.infrastructure.adapter.out.persistence.mapper;

import com.adtech.clickaggregator.domain.model.ClickAggregation;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.entity.ClickAggregationEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between ClickAggregation domain model and JPA entity
 */
@Component
public class ClickAggregationEntityMapper {

    public ClickAggregationEntity toEntity(ClickAggregation domain) {
        return ClickAggregationEntity.builder()
                .aggregationId(domain.getAggregationId())
                .dimension(domain.getDimension())
                .dimensionValue(domain.getDimensionValue())
                .timeBucket(domain.getTimeBucket())
                .clickCount(domain.getClickCount())
                .totalCost(domain.getTotalCost())
                .uniqueUsers(domain.getUniqueUsers())
                .lastUpdated(domain.getLastUpdated())
                .build();
    }

    public ClickAggregation toDomain(ClickAggregationEntity entity) {
        return ClickAggregation.builder()
                .aggregationId(entity.getAggregationId())
                .dimension(entity.getDimension())
                .dimensionValue(entity.getDimensionValue())
                .timeBucket(entity.getTimeBucket())
                .clickCount(entity.getClickCount())
                .totalCost(entity.getTotalCost())
                .uniqueUsers(entity.getUniqueUsers())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }

    public List<ClickAggregation> toDomainList(List<ClickAggregationEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<ClickAggregationEntity> toEntityList(List<ClickAggregation> domains) {
        return domains.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
