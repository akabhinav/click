package com.adtech.clickaggregator.infrastructure.adapter.out.persistence;

import com.adtech.clickaggregator.domain.model.AggregationDimension;
import com.adtech.clickaggregator.domain.model.AggregationQuery;
import com.adtech.clickaggregator.domain.model.ClickAggregation;
import com.adtech.clickaggregator.domain.port.out.AggregationRepository;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.entity.ClickAggregationEntity;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.mapper.ClickAggregationEntityMapper;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.repository.JpaClickAggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of AggregationRepository port
 */
@Slf4j
@Component
public class AggregationRepositoryImpl implements AggregationRepository {

    private final JpaClickAggregationRepository jpaRepository;
    private final ClickAggregationEntityMapper mapper;

    public AggregationRepositoryImpl(
            JpaClickAggregationRepository jpaRepository,
            ClickAggregationEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ClickAggregation save(ClickAggregation aggregation) {
        ClickAggregationEntity entity = mapper.toEntity(aggregation);
        ClickAggregationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<ClickAggregation> saveAll(List<ClickAggregation> aggregations) {
        List<ClickAggregationEntity> entities = mapper.toEntityList(aggregations);
        List<ClickAggregationEntity> saved = jpaRepository.saveAll(entities);
        return mapper.toDomainList(saved);
    }

    @Override
    public Optional<ClickAggregation> findById(String aggregationId) {
        return jpaRepository.findById(aggregationId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ClickAggregation> findByQuery(AggregationQuery query) {
        List<ClickAggregationEntity> entities;

        if (query.getDimensionValues() != null && !query.getDimensionValues().isEmpty()) {
            entities = jpaRepository.findByDimensionAndDimensionValueInAndTimeBucketBetween(
                    query.getDimension(),
                    query.getDimensionValues(),
                    query.getStartTime(),
                    query.getEndTime()
            );
        } else {
            entities = jpaRepository.findByDimensionAndTimeBucketBetween(
                    query.getDimension(),
                    query.getStartTime(),
                    query.getEndTime()
            );
        }

        return mapper.toDomainList(entities);
    }

    @Override
    public List<ClickAggregation> findTopByDimension(AggregationDimension dimension, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);

        List<ClickAggregationEntity> entities;
        if (dimension == AggregationDimension.CAMPAIGN) {
            entities = jpaRepository.findTopByDimensionOrderByTotalCost(dimension, pageRequest);
        } else {
            entities = jpaRepository.findTopByDimensionOrderByClickCount(dimension, pageRequest);
        }

        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public void incrementCounters(String aggregationId, long clickCount, double cost) {
        jpaRepository.findById(aggregationId).ifPresent(entity -> {
            entity.setClickCount(entity.getClickCount() + clickCount);
            entity.setTotalCost(entity.getTotalCost() + cost);
            entity.setLastUpdated(Instant.now());
            jpaRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public long deleteOlderThan(Instant cutoffTime) {
        return jpaRepository.deleteOlderThan(cutoffTime);
    }
}
