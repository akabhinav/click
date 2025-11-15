# Architecture Documentation

## Overview

The Ad Click Aggregator is built using **Hexagonal Architecture** (also known as Ports and Adapters), which provides clean separation of concerns, testability, and flexibility to swap infrastructure components.

## Architectural Principles

### 1. Clean Architecture

- **Independence**: Business logic is independent of frameworks, UI, database, and external services
- **Testability**: Business rules can be tested without external dependencies
- **Flexibility**: Easy to swap implementations (e.g., PostgreSQL → MongoDB)

### 2. Domain-Driven Design (DDD)

- **Ubiquitous Language**: Code reflects business terminology
- **Domain Models**: Rich domain models with business logic
- **Aggregates**: ClickEvent and ClickAggregation as core aggregates

### 3. CQRS (Command Query Responsibility Segregation)

- **Commands**: Write operations (ProcessClickEvent)
- **Queries**: Read operations (QueryAggregation)
- Separate optimization paths for reads and writes

## Layer Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Presentation/API Layer                        │
│                     (REST Controllers)                           │
└────────────────────────────┬────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Application Layer                           │
│                   (Use Cases & Orchestration)                    │
│                                                                   │
│  ┌────────────────────┐        ┌─────────────────────────┐      │
│  │ ProcessClickEvent  │        │  QueryAggregation       │      │
│  │      UseCase       │        │      UseCase            │      │
│  └────────────────────┘        └─────────────────────────┘      │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │           AggregationProcessor Service                   │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Domain Layer                              │
│                   (Pure Business Logic)                          │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │  ClickEvent  │  │ClickAggregation│ │ AggregationService │   │
│  └──────────────┘  └──────────────┘  └────────────────────┘    │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Ports (Interfaces)                          │   │
│  │  ┌────────────────┐    ┌──────────────────────────┐     │   │
│  │  │  Inbound Ports │    │    Outbound Ports        │     │   │
│  │  │  (Use Cases)   │    │  (Repository, Cache,     │     │   │
│  │  │                │    │   EventPublisher)        │     │   │
│  │  └────────────────┘    └──────────────────────────┘     │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                           │
│                    (Technical Details)                           │
│                                                                   │
│  ┌─────────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │  PostgreSQL     │  │    Redis     │  │      Kafka       │   │
│  │  Adapter        │  │   Adapter    │  │     Adapter      │   │
│  └─────────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## Component Details

### Domain Layer

#### Models
- **ClickEvent**: Represents a single ad click with metadata
- **ClickAggregation**: Aggregated statistics by dimension and time
- **AggregationDimension**: Enum for aggregation types (AD, CAMPAIGN, COUNTRY, DEVICE_TYPE)
- **AggregationQuery**: Value object for query parameters

#### Domain Services
- **AggregationService**: Business logic for creating and merging aggregations

#### Ports (Interfaces)
**Inbound Ports** (driven by external actors):
- `ProcessClickEventUseCase`: Process incoming click events
- `QueryAggregationUseCase`: Query aggregated data

**Outbound Ports** (driven by application):
- `ClickEventRepository`: Persist click events
- `AggregationRepository`: Persist aggregations
- `EventPublisher`: Publish events to message broker
- `CacheService`: Cache operations

### Application Layer

#### Use Cases
- **ProcessClickEventUseCaseImpl**: Validates and processes click events
- **QueryAggregationUseCaseImpl**: Queries aggregations with caching

#### Services
- **AggregationProcessor**: Core aggregation engine
  - Consumes events from Kafka
  - Updates aggregations atomically
  - Maintains cache consistency

### Infrastructure Layer

#### Inbound Adapters
- **ClickEventController**: REST API for click ingestion
- **AggregationController**: REST API for analytics
- **ClickEventConsumer**: Kafka consumer for async processing

#### Outbound Adapters
- **ClickEventRepositoryImpl**: JPA implementation
- **AggregationRepositoryImpl**: JPA implementation
- **RedisCacheService**: Redis implementation
- **KafkaEventPublisher**: Kafka producer implementation

## Data Flow

### Write Path (Click Ingestion)

```
Client Request
      │
      ▼
[REST Controller]
      │
      ▼
[ProcessClickEventUseCase]
      │
      ├──► [EventPublisher] ──► Kafka Topic
      │
      ▼
[Return Response]

(Asynchronously)
Kafka Topic
      │
      ▼
[ClickEventConsumer]
      │
      ▼
[AggregationProcessor]
      │
      ├──► [ClickEventRepository] ──► PostgreSQL
      │
      └──► [AggregationRepository] ──► PostgreSQL
            └──► [CacheService] ──► Redis
```

### Read Path (Analytics Query)

```
Client Request
      │
      ▼
[AggregationController]
      │
      ▼
[QueryAggregationUseCase]
      │
      ├──► Check [CacheService] ──► Redis
      │         │
      │         ├─► Cache Hit ──► Return Cached Data
      │         │
      │         └─► Cache Miss
      │                  │
      │                  ▼
      └──► [AggregationRepository] ──► PostgreSQL
                  │
                  ▼
            [Update Cache] ──► Redis
                  │
                  ▼
            Return Fresh Data
```

## Scalability Design

### Horizontal Scaling

1. **Stateless Application Tier**
   - Multiple instances behind load balancer
   - No session state stored in memory
   - All state in database/cache

2. **Kafka Partitioning**
   - Events partitioned by campaign_id
   - Parallel processing across consumers
   - Consumer group coordination

3. **Database Optimization**
   - Strategic indexes on query columns
   - Read replicas for analytics queries
   - Connection pooling

4. **Caching Strategy**
   - Redis for hot aggregations
   - TTL-based cache invalidation
   - Cache-aside pattern

### Vertical Scaling

1. **Batch Processing**
   - Hibernate batch inserts (batch_size: 50)
   - JDBC batching enabled
   - Reduced database round trips

2. **Connection Pooling**
   - HikariCP connection pool
   - Optimized pool size based on load

3. **Async Processing**
   - Thread pool for async tasks
   - Kafka consumer concurrency (3 threads)

## Performance Optimizations

### Database

```sql
-- Strategic indexes
CREATE INDEX idx_campaign_timestamp ON click_events(campaign_id, timestamp);
CREATE INDEX idx_ad_timestamp ON click_events(ad_id, timestamp);
CREATE INDEX idx_dimension_value_time ON click_aggregations(dimension, dimension_value, time_bucket);
CREATE INDEX idx_click_count ON click_aggregations(click_count DESC);
```

### Caching

- **Aggregation Cache**: 10-minute TTL
- **Query Cache**: 5-minute TTL
- **Write-through cache** for real-time aggregations

### Kafka

- **Producer**:
  - Batch size: 32KB
  - Linger time: 10ms
  - Compression: Snappy
  - Acks: 1 (leader only)

- **Consumer**:
  - Max poll records: 500
  - Fetch min bytes: 1KB
  - Manual acknowledgment

## Security Considerations

### Current Implementation

- Input validation with Bean Validation
- SQL injection prevention via JPA
- No authentication (demo/development)

### Production Recommendations

1. **Authentication & Authorization**
   - OAuth 2.0 / JWT tokens
   - Role-based access control (RBAC)
   - API rate limiting

2. **Network Security**
   - HTTPS/TLS for all endpoints
   - Kafka SSL/SASL authentication
   - Database connection encryption

3. **Data Protection**
   - PII data encryption at rest
   - Sensitive data masking in logs
   - GDPR compliance considerations

## Monitoring & Observability

### Metrics Collected

- **Business Metrics**:
  - `clicks.processed`: Total clicks processed
  - `clicks.invalid`: Invalid clicks rejected
  - `aggregations.processed`: Aggregations computed

- **Technical Metrics**:
  - JVM metrics (heap, GC)
  - Database connection pool
  - HTTP request rates and latencies
  - Kafka consumer lag

### Health Checks

- Database connectivity
- Redis connectivity
- Kafka producer availability
- Disk space
- Memory usage

## Testing Strategy

### Unit Tests
- Domain model validation
- Business logic correctness
- No external dependencies

### Integration Tests
- API contract testing
- Database integration
- Kafka integration

### Load Tests
- Throughput testing
- Latency benchmarking
- Stress testing

## Future Enhancements

### Phase 2
- [ ] GraphQL API
- [ ] WebSocket for real-time updates
- [ ] Machine learning for fraud detection
- [ ] Advanced analytics (cohort analysis)

### Phase 3
- [ ] Multi-region deployment
- [ ] Data lake integration (S3/BigQuery)
- [ ] Custom dashboard UI
- [ ] A/B testing framework

## Design Patterns Used

1. **Hexagonal Architecture**: Core architectural pattern
2. **Repository Pattern**: Data access abstraction
3. **Factory Pattern**: ClickEvent creation
4. **Strategy Pattern**: Multiple aggregation dimensions
5. **Template Method**: Base repository implementations
6. **Observer Pattern**: Event-driven processing
7. **Cache-Aside Pattern**: Caching strategy

## Technology Decisions

### Why PostgreSQL?
- ACID compliance for financial data
- Excellent query performance
- Mature ecosystem
- Good analytical capabilities

### Why Redis?
- In-memory speed for hot data
- Atomic operations for counters
- Simple key-value for caching
- Pub/sub for real-time updates

### Why Kafka?
- High throughput for event streaming
- Durability and replayability
- Horizontal scalability
- Ecosystem integration

### Why Spring Boot?
- Production-ready features
- Extensive ecosystem
- Auto-configuration
- Excellent monitoring support

## References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/tags/domain%20driven%20design.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Microservices Patterns](https://microservices.io/patterns/index.html)
