package com.adtech.clickaggregator.domain.port.in;

import com.adtech.clickaggregator.domain.model.AggregationQuery;
import com.adtech.clickaggregator.domain.model.ClickAggregation;

import java.util.List;

/**
 * Inbound port for querying aggregated click data
 */
public interface QueryAggregationUseCase {

    /**
     * Query aggregations based on specified criteria
     *
     * @param query the aggregation query
     * @return list of matching aggregations
     */
    List<ClickAggregation> queryAggregations(AggregationQuery query);

    /**
     * Get top performing ads by click count
     *
     * @param limit number of results
     * @return list of top aggregations
     */
    List<ClickAggregation> getTopAds(int limit);

    /**
     * Get top performing campaigns by cost
     *
     * @param limit number of results
     * @return list of top campaigns
     */
    List<ClickAggregation> getTopCampaigns(int limit);
}
