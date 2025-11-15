package com.adtech.clickaggregator.domain.service;

import com.adtech.clickaggregator.domain.model.AggregationDimension;
import com.adtech.clickaggregator.domain.model.ClickAggregation;
import com.adtech.clickaggregator.domain.model.ClickEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain service for aggregation logic.
 * Contains pure business logic for creating and combining aggregations.
 */
@Service
public class AggregationService {

    /**
     * Create aggregations for all dimensions from a single click event
     *
     * @param event the click event
     * @return list of aggregations for each dimension
     */
    public List<ClickAggregation> createAggregations(ClickEvent event) {
        List<ClickAggregation> aggregations = new ArrayList<>();

        for (AggregationDimension dimension : AggregationDimension.values()) {
            if (shouldAggregate(event, dimension)) {
                aggregations.add(ClickAggregation.fromClickEvent(event, dimension));
            }
        }

        return aggregations;
    }

    /**
     * Merge multiple aggregations with the same key
     *
     * @param aggregations list of aggregations to merge
     * @return merged aggregation
     */
    public ClickAggregation mergeAggregations(List<ClickAggregation> aggregations) {
        if (aggregations == null || aggregations.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge empty list");
        }

        ClickAggregation result = aggregations.get(0);
        for (int i = 1; i < aggregations.size(); i++) {
            result.merge(aggregations.get(i));
        }

        return result;
    }

    /**
     * Check if an event should be aggregated for a given dimension
     */
    private boolean shouldAggregate(ClickEvent event, AggregationDimension dimension) {
        return switch (dimension) {
            case AD -> event.getAdId() != null && !event.getAdId().isBlank();
            case CAMPAIGN -> event.getCampaignId() != null && !event.getCampaignId().isBlank();
            case COUNTRY -> event.getCountry() != null && !event.getCountry().isBlank();
            case DEVICE_TYPE -> event.getDeviceType() != null && !event.getDeviceType().isBlank();
        };
    }

    /**
     * Validate if aggregation data is consistent
     */
    public boolean isValidAggregation(ClickAggregation aggregation) {
        return aggregation != null
                && aggregation.getDimension() != null
                && aggregation.getDimensionValue() != null
                && aggregation.getTimeBucket() != null
                && aggregation.getClickCount() >= 0
                && aggregation.getTotalCost() >= 0;
    }
}
