package com.adtech.clickaggregator.infrastructure.adapter.in.rest.mapper;

import com.adtech.clickaggregator.domain.model.ClickAggregation;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto.AggregationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between ClickAggregation domain model and REST DTOs
 */
@Component
public class AggregationMapper {

    public AggregationResponse toResponse(ClickAggregation aggregation) {
        return AggregationResponse.builder()
                .dimension(aggregation.getDimension().name())
                .dimensionValue(aggregation.getDimensionValue())
                .timeBucket(aggregation.getTimeBucket())
                .clickCount(aggregation.getClickCount())
                .totalCost(aggregation.getTotalCost())
                .uniqueUsers(aggregation.getUniqueUsers())
                .averageCostPerClick(aggregation.getAverageCostPerClick())
                .lastUpdated(aggregation.getLastUpdated())
                .build();
    }

    public List<AggregationResponse> toResponseList(List<ClickAggregation> aggregations) {
        return aggregations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
