package com.adtech.clickaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Ad Click Aggregator.
 *
 * A world-class, scalable platform for processing millions of ad click events
 * with real-time aggregation and analytics capabilities.
 *
 * Architecture: Hexagonal Architecture (Ports & Adapters)
 * - Domain Layer: Core business logic
 * - Application Layer: Use cases and orchestration
 * - Infrastructure Layer: Adapters for external systems
 *
 * Key Features:
 * - High-throughput click event ingestion via REST API
 * - Async event processing with Kafka
 * - Real-time aggregation with Redis caching
 * - Multiple aggregation dimensions (ad, campaign, country, device)
 * - Comprehensive analytics and reporting APIs
 * - Production-ready monitoring and metrics
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class ClickAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClickAggregatorApplication.class, args);
    }
}
