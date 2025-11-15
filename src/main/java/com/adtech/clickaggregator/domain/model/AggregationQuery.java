package com.adtech.clickaggregator.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Value object representing a query for aggregated click data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationQuery {

    private AggregationDimension dimension;
    private List<String> dimensionValues;
    private Instant startTime;
    private Instant endTime;
    private Integer limit;
    private Integer offset;

    /**
     * Validates the query parameters
     */
    public boolean isValid() {
        return dimension != null
                && startTime != null
                && endTime != null
                && startTime.isBefore(endTime)
                && (limit == null || limit > 0)
                && (offset == null || offset >= 0);
    }
}
