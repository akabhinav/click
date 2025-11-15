package com.adtech.clickaggregator.infrastructure.adapter.out.persistence;

import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.adtech.clickaggregator.domain.port.out.ClickEventRepository;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.entity.ClickEventEntity;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.mapper.ClickEventEntityMapper;
import com.adtech.clickaggregator.infrastructure.adapter.out.persistence.repository.JpaClickEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of ClickEventRepository port
 */
@Slf4j
@Component
public class ClickEventRepositoryImpl implements ClickEventRepository {

    private final JpaClickEventRepository jpaRepository;
    private final ClickEventEntityMapper mapper;

    public ClickEventRepositoryImpl(
            JpaClickEventRepository jpaRepository,
            ClickEventEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ClickEvent save(ClickEvent event) {
        ClickEventEntity entity = mapper.toEntity(event);
        ClickEventEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<ClickEvent> saveAll(List<ClickEvent> events) {
        List<ClickEventEntity> entities = mapper.toEntityList(events);
        List<ClickEventEntity> saved = jpaRepository.saveAll(entities);
        return mapper.toDomainList(saved);
    }

    @Override
    public Optional<ClickEvent> findById(String eventId) {
        return jpaRepository.findById(eventId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ClickEvent> findByTimeRange(Instant startTime, Instant endTime) {
        List<ClickEventEntity> entities = jpaRepository.findByTimestampBetween(startTime, endTime);
        return mapper.toDomainList(entities);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    @Transactional
    public long deleteOlderThan(Instant cutoffTime) {
        return jpaRepository.deleteOlderThan(cutoffTime);
    }
}
