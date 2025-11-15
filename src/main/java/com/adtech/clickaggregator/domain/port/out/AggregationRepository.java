package com.adtech.clickaggregator.domain.port.out;

import com.adtech.clickaggregator.domain.model.AggregationDimension;
import com.adtech.clickaggregator.domain.model.AggregationQuery;
import com.adtech.clickaggregator.domain.model.ClickAggregation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for aggregation persistence
 */
public interface AggregationRepository {

    /**
     * Save or update an aggregation
     *
     * @param aggregation the aggregation to save
     * @return the saved aggregation
     */
    ClickAggregation save(ClickAggregation aggregation);

    /**
     * Save multiple aggregations in batch
     *
     * @param aggregations list of aggregations
     * @return list of saved aggregations
     */
    List<ClickAggregation> saveAll(List<ClickAggregation> aggregations);

    /**
     * Find an aggregation by ID
     *
     * @param aggregationId the aggregation ID
     * @return the aggregation if found
     */
    Optional<ClickAggregation> findById(String aggregationId);

    /**
     * Find aggregations matching a query
     *
     * @param query the query criteria
     * @return list of matching aggregations
     */
    List<ClickAggregation> findByQuery(AggregationQuery query);

    /**
     * Find top N aggregations by dimension
     *
     * @param dimension the aggregation dimension
     * @param limit number of results
     * @return list of top aggregations
     */
    List<ClickAggregation> findTopByDimension(AggregationDimension dimension, int limit);

    /**
     * Increment aggregation counters atomically
     *
     * @param aggregationId the aggregation ID
     * @param clickCount clicks to add
     * @param cost cost to add
     */
    void incrementCounters(String aggregationId, long clickCount, double cost);

    /**
     * Delete aggregations older than specified time
     *
     * @param cutoffTime the cutoff time
     * @return number of deleted aggregations
     */
    long deleteOlderThan(Instant cutoffTime);
}
