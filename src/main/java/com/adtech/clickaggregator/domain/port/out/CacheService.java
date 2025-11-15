package com.adtech.clickaggregator.domain.port.out;

import java.time.Duration;
import java.util.Optional;

/**
 * Outbound port for caching operations
 */
public interface CacheService {

    /**
     * Store a value in cache
     *
     * @param key the cache key
     * @param value the value to cache
     * @param ttl time to live
     */
    void put(String key, Object value, Duration ttl);

    /**
     * Retrieve a value from cache
     *
     * @param key the cache key
     * @param type the value type
     * @return the cached value if present
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Remove a value from cache
     *
     * @param key the cache key
     */
    void evict(String key);

    /**
     * Increment a counter in cache atomically
     *
     * @param key the counter key
     * @param delta amount to increment
     * @return new value
     */
    long increment(String key, long delta);

    /**
     * Check if a key exists in cache
     *
     * @param key the cache key
     * @return true if exists
     */
    boolean exists(String key);
}
