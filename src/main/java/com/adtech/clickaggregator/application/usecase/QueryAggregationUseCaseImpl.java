package com.adtech.clickaggregator.application.usecase;

import com.adtech.clickaggregator.domain.model.AggregationDimension;
import com.adtech.clickaggregator.domain.model.AggregationQuery;
import com.adtech.clickaggregator.domain.model.ClickAggregation;
import com.adtech.clickaggregator.domain.port.in.QueryAggregationUseCase;
import com.adtech.clickaggregator.domain.port.out.AggregationRepository;
import com.adtech.clickaggregator.domain.port.out.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of aggregation query use case.
 * Handles querying with caching for performance.
 */
@Slf4j
@Service
public class QueryAggregationUseCaseImpl implements QueryAggregationUseCase {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final String CACHE_PREFIX = "aggregation:";

    private final AggregationRepository aggregationRepository;
    private final CacheService cacheService;

    public QueryAggregationUseCaseImpl(
            AggregationRepository aggregationRepository,
            CacheService cacheService) {
        this.aggregationRepository = aggregationRepository;
        this.cacheService = cacheService;
    }

    @Override
    public List<ClickAggregation> queryAggregations(AggregationQuery query) {
        log.debug("Querying aggregations: {}", query);

        if (!query.isValid()) {
            throw new IllegalArgumentException("Invalid aggregation query");
        }

        // Try cache first
        String cacheKey = buildCacheKey(query);
        Optional<List<ClickAggregation>> cached = cacheService.get(cacheKey, List.class);

        if (cached.isPresent()) {
            log.debug("Cache hit for query: {}", cacheKey);
            return cached.get();
        }

        // Query from database
        List<ClickAggregation> results = aggregationRepository.findByQuery(query);

        // Cache results
        cacheService.put(cacheKey, results, CACHE_TTL);

        log.info("Retrieved {} aggregations for query", results.size());
        return results;
    }

    @Override
    public List<ClickAggregation> getTopAds(int limit) {
        log.debug("Fetching top {} ads", limit);

        String cacheKey = CACHE_PREFIX + "top:ads:" + limit;
        Optional<List<ClickAggregation>> cached = cacheService.get(cacheKey, List.class);

        if (cached.isPresent()) {
            return cached.get();
        }

        List<ClickAggregation> results = aggregationRepository.findTopByDimension(
                AggregationDimension.AD, limit);

        cacheService.put(cacheKey, results, CACHE_TTL);
        return results;
    }

    @Override
    public List<ClickAggregation> getTopCampaigns(int limit) {
        log.debug("Fetching top {} campaigns", limit);

        String cacheKey = CACHE_PREFIX + "top:campaigns:" + limit;
        Optional<List<ClickAggregation>> cached = cacheService.get(cacheKey, List.class);

        if (cached.isPresent()) {
            return cached.get();
        }

        List<ClickAggregation> results = aggregationRepository.findTopByDimension(
                AggregationDimension.CAMPAIGN, limit);

        cacheService.put(cacheKey, results, CACHE_TTL);
        return results;
    }

    private String buildCacheKey(AggregationQuery query) {
        return String.format("%squery:%s:%d:%d",
                CACHE_PREFIX,
                query.getDimension(),
                query.getStartTime().getEpochSecond(),
                query.getEndTime().getEpochSecond());
    }
}
