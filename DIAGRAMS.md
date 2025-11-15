# Architecture Diagrams & Flows

## Table of Contents
1. [High-Level System Architecture](#1-high-level-system-architecture)
2. [Hexagonal Architecture Layers](#2-hexagonal-architecture-layers)
3. [Write Flow - Click Event Ingestion](#3-write-flow---click-event-ingestion)
4. [Read Flow - Analytics Query](#4-read-flow---analytics-query)
5. [Async Processing Flow](#5-async-processing-flow)
6. [Component Interaction Diagram](#6-component-interaction-diagram)
7. [Deployment Architecture](#7-deployment-architecture)
8. [Data Model Relationships](#8-data-model-relationships)
9. [Kafka Partitioning Strategy](#9-kafka-partitioning-strategy)
10. [Caching Strategy](#10-caching-strategy)

---

## 1. High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              EXTERNAL CLIENTS                            │
│                    (Web Apps, Mobile Apps, Ad Networks)                  │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 │ HTTPS
                                 │
┌────────────────────────────────▼────────────────────────────────────────┐
│                          LOAD BALANCER (Optional)                        │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                 ┌───────────────┼───────────────┐
                 │               │               │
┌────────────────▼───┐  ┌────────▼──────┐  ┌────▼───────────────┐
│  App Instance 1    │  │ App Instance 2│  │  App Instance N    │
│                    │  │               │  │                    │
│ ┌────────────────┐ │  │               │  │                    │
│ │ REST API       │ │  │  (Stateless   │  │   (Horizontal      │
│ │ - /clicks      │ │  │   Instances)  │  │    Scalable)       │
│ │ - /aggregations│ │  │               │  │                    │
│ └────────┬───────┘ │  │               │  │                    │
│          │         │  │               │  │                    │
│ ┌────────▼───────┐ │  │               │  │                    │
│ │  Use Cases     │ │  │               │  │                    │
│ └────────┬───────┘ │  │               │  │                    │
│          │         │  │               │  │                    │
│ ┌────────▼───────┐ │  │               │  │                    │
│ │ Domain Logic   │ │  │               │  │                    │
│ └────────┬───────┘ │  │               │  │                    │
└──────────┼─────────┘  └───────────────┘  └────────────────────┘
           │
           │
           ├──────────────┬──────────────┬──────────────┐
           │              │              │              │
┌──────────▼────────┐ ┌───▼─────────┐ ┌──▼──────────┐ ┌▼────────────────┐
│  Apache Kafka     │ │ PostgreSQL  │ │   Redis     │ │   Prometheus    │
│                   │ │             │ │             │ │                 │
│ ┌───────────────┐ │ │ ┌─────────┐ │ │ ┌─────────┐ │ │  (Monitoring)   │
│ │click-events   │ │ │ │ Events  │ │ │ │  Cache  │ │ │                 │
│ │topic          │ │ │ │  Table  │ │ │ │         │ │ └─────────────────┘
│ │               │ │ │ │         │ │ │ │         │ │
│ │[P1][P2][P3]   │ │ │ │Aggreg.  │ │ │ │ Hot Data│ │
│ └───────────────┘ │ │ │ Table   │ │ │ │         │ │
│                   │ │ └─────────┘ │ │ └─────────┘ │
│ Event Streaming   │ │ Persistence │ │ In-Memory   │
└───────────────────┘ └─────────────┘ └─────────────┘
```

---

## 2. Hexagonal Architecture Layers

```
┌───────────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE LAYER                              │
│                   (Frameworks & External Systems)                      │
│                                                                        │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    INBOUND ADAPTERS                             │  │
│  │                                                                 │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐   │  │
│  │  │ REST API     │  │ Kafka        │  │  Scheduled Jobs    │   │  │
│  │  │ Controllers  │  │ Consumers    │  │  (Future)          │   │  │
│  │  │              │  │              │  │                    │   │  │
│  │  │ - ClickEvent │  │ - ClickEvent │  │ - Batch           │   │  │
│  │  │   Controller │  │   Consumer   │  │   Processing      │   │  │
│  │  │ - Aggregation│  │              │  │                    │   │  │
│  │  │   Controller │  │              │  │                    │   │  │
│  │  └──────┬───────┘  └──────┬───────┘  └────────┬───────────┘   │  │
│  │         │                  │                   │               │  │
│  └─────────┼──────────────────┼───────────────────┼───────────────┘  │
│            │                  │                   │                  │
│  ┌─────────▼──────────────────▼───────────────────▼───────────────┐  │
│  │                     PORT INTERFACES                            │  │
│  │  (Contracts between Infrastructure & Application)             │  │
│  └────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬───────────────────────────────────────┘
                                │
┌───────────────────────────────▼───────────────────────────────────────┐
│                       APPLICATION LAYER                                │
│                  (Use Cases & Orchestration)                           │
│                                                                        │
│  ┌──────────────────────────┐      ┌───────────────────────────────┐  │
│  │ ProcessClickEventUseCase │      │  QueryAggregationUseCase      │  │
│  │                          │      │                               │  │
│  │ - Validate events        │      │  - Query with caching         │  │
│  │ - Publish to Kafka       │      │  - Top ads/campaigns          │  │
│  │ - Return acknowledgment  │      │  - Time-based filtering       │  │
│  └──────────┬───────────────┘      └───────────┬───────────────────┘  │
│             │                                   │                      │
│  ┌──────────▼───────────────────────────────────▼───────────────────┐ │
│  │              Application Services                                │ │
│  │                                                                  │ │
│  │  ┌────────────────────────────────────────────────────────────┐ │ │
│  │  │           AggregationProcessor                             │ │ │
│  │  │                                                            │ │ │
│  │  │  - Process click events from Kafka                        │ │ │
│  │  │  - Create aggregations for all dimensions                 │ │ │
│  │  │  - Update/merge existing aggregations                     │ │ │
│  │  │  - Manage cache consistency                               │ │ │
│  │  └────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────┬───────────────────────────────────────┘
                                │
┌───────────────────────────────▼───────────────────────────────────────┐
│                         DOMAIN LAYER                                   │
│                   (Pure Business Logic)                                │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                     DOMAIN MODELS                              │   │
│  │                                                                │   │
│  │  ┌──────────────┐  ┌─────────────────┐  ┌──────────────────┐ │   │
│  │  │  ClickEvent  │  │ClickAggregation │  │AggregationQuery  │ │   │
│  │  │              │  │                 │  │                  │ │   │
│  │  │ - eventId    │  │ - dimension     │  │ - dimension      │ │   │
│  │  │ - adId       │  │ - clickCount    │  │ - startTime      │ │   │
│  │  │ - campaignId │  │ - totalCost     │  │ - endTime        │ │   │
│  │  │ - timestamp  │  │ - uniqueUsers   │  │ - limit          │ │   │
│  │  │ - cost       │  │ - timeBucket    │  │                  │ │   │
│  │  │              │  │                 │  │                  │ │   │
│  │  │ + isValid()  │  │ + merge()       │  │ + isValid()      │ │   │
│  │  │ + getHour()  │  │ + getAvgCost()  │  │                  │ │   │
│  │  └──────────────┘  └─────────────────┘  └──────────────────┘ │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                   DOMAIN SERVICES                              │   │
│  │                                                                │   │
│  │  ┌──────────────────────────────────────────────────────────┐ │   │
│  │  │         AggregationService                               │ │   │
│  │  │                                                          │ │   │
│  │  │  + createAggregations(ClickEvent)                       │ │   │
│  │  │  + mergeAggregations(List<Aggregation>)                 │ │   │
│  │  │  + isValidAggregation(Aggregation)                      │ │   │
│  │  └──────────────────────────────────────────────────────────┘ │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                    DOMAIN PORTS                                │   │
│  │                                                                │   │
│  │  Inbound (Driven):                 Outbound (Driving):        │   │
│  │  ┌──────────────────────┐          ┌────────────────────────┐ │   │
│  │  │ ProcessClickEvent    │          │ ClickEventRepository   │ │   │
│  │  │ QueryAggregation     │          │ AggregationRepository  │ │   │
│  │  └──────────────────────┘          │ EventPublisher         │ │   │
│  │                                    │ CacheService           │ │   │
│  │                                    └────────────────────────┘ │   │
│  └────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬───────────────────────────────────────┘
                                │
┌───────────────────────────────▼───────────────────────────────────────┐
│                      INFRASTRUCTURE LAYER                              │
│                     (Technical Implementation)                         │
│                                                                        │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    OUTBOUND ADAPTERS                            │  │
│  │                                                                 │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐ │  │
│  │  │ PostgreSQL   │  │    Redis     │  │     Kafka            │ │  │
│  │  │ Adapter      │  │   Adapter    │  │     Adapter          │ │  │
│  │  │              │  │              │  │                      │ │  │
│  │  │ - JPA Repos  │  │ - Cache Impl │  │ - Event Publisher    │ │  │
│  │  │ - Entities   │  │ - Redis      │  │ - Producer Config    │ │  │
│  │  │ - Mappers    │  │   Template   │  │                      │ │  │
│  │  └──────────────┘  └──────────────┘  └──────────────────────┘ │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
```

---

## 3. Write Flow - Click Event Ingestion

### 3.1 Synchronous Path (Direct Save)

```
┌──────────┐
│  Client  │
└────┬─────┘
     │
     │ POST /api/v1/clicks
     │ {ad_id, campaign_id, cost, ...}
     │
     ▼
┌────────────────────────────────────────┐
│  ClickEventController                  │
│  (REST API Entry Point)                │
│                                        │
│  1. Validate JSON                      │
│  2. Create ClickEventRequest DTO       │
└────┬───────────────────────────────────┘
     │
     │ toDomain(request)
     │
     ▼
┌────────────────────────────────────────┐
│  ClickEventMapper                      │
│                                        │
│  - Convert DTO → Domain Model          │
│  - Generate event ID                   │
│  - Set timestamp                       │
└────┬───────────────────────────────────┘
     │
     │ ClickEvent
     │
     ▼
┌────────────────────────────────────────┐
│  ProcessClickEventUseCase              │
│                                        │
│  1. Validate event (isValid())         │
│  2. Increment metrics counter          │
│  3. Choose processing path             │
└────┬───────────────────────────────────┘
     │
     ├─────────── Async Path ──────────┐
     │                                 │
     │ processClick()                  │ processClickSync()
     │                                 │
     ▼                                 ▼
┌────────────────────┐         ┌──────────────────┐
│  EventPublisher    │         │ClickEventRepo    │
│  (Kafka)           │         │                  │
│                    │         │ Direct Save      │
│ 1. Serialize event │         │ to PostgreSQL    │
│ 2. Send to topic   │         └──────────────────┘
│ 3. Partition by    │
│    campaign_id     │
└────┬───────────────┘
     │
     │ Async (fire & forget)
     │
     ▼
┌────────────────────────────────────────┐
│  Kafka Broker                          │
│                                        │
│  Topic: click-events                   │
│  ┌──────┐  ┌──────┐  ┌──────┐        │
│  │ P0   │  │ P1   │  │ P2   │        │
│  │ camp │  │ camp │  │ camp │        │
│  │ -001 │  │ -002 │  │ -003 │        │
│  └──────┘  └──────┘  └──────┘        │
└────┬───────────────────────────────────┘
     │
     │ Consumer Group: click-aggregator-consumer
     │
     ▼
┌────────────────────────────────────────┐
│  ClickEventConsumer                    │
│  (Kafka Consumer - 3 instances)        │
│                                        │
│  1. Consume from partition             │
│  2. Deserialize JSON                   │
│  3. Call AggregationProcessor          │
│  4. Manual acknowledge                 │
└────┬───────────────────────────────────┘
     │
     │ processClickEvent(event)
     │
     ▼
┌────────────────────────────────────────┐
│  AggregationProcessor                  │
│  (Core Business Logic)                 │
│                                        │
│  1. Save raw event to DB               │
│  2. Create aggregations (4 dimensions) │
│  3. For each aggregation:              │
│     - Check cache for existing         │
│     - If exists: merge                 │
│     - If not: create new               │
│     - Save to DB                       │
│     - Update cache                     │
└────┬───────────────────────────────────┘
     │
     ├────────┬────────┬────────┐
     │        │        │        │
     ▼        ▼        ▼        ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│  AD    │ │Campaign│ │Country │ │ Device │
│  Agg   │ │  Agg   │ │  Agg   │ │  Agg   │
└────┬───┘ └────┬───┘ └────┬───┘ └────┬───┘
     │          │          │          │
     └──────────┴──────────┴──────────┘
                │
     ┌──────────┴───────────┐
     │                      │
     ▼                      ▼
┌──────────────┐    ┌──────────────┐
│ PostgreSQL   │    │    Redis     │
│              │    │              │
│ - Events     │    │ - Hot Data   │
│ - Aggreg.    │    │ - TTL Cache  │
└──────────────┘    └──────────────┘
```

### 3.2 Detailed Event Flow with Timing

```
Time    Component              Action                           Duration
────    ─────────────────────  ──────────────────────────────   ────────
0ms     Client                 Send POST request                 -
        │
2ms     Load Balancer          Route to instance                 2ms
        │
3ms     REST Controller        Receive & validate                1ms
        │
4ms     Mapper                 DTO → Domain                      1ms
        │
5ms     Use Case               Validate business rules           1ms
        │
6ms     Event Publisher        Serialize & send to Kafka         1ms
        │
7ms     Controller             Return 202 Accepted               1ms
        │
        └──────────────────────────────────────────────────────────
        CLIENT RECEIVES RESPONSE (7ms)
        ══════════════════════════════════════════════════════════

        ASYNC PROCESSING BEGINS

50ms    Kafka Broker           Event persisted in topic          43ms
        │
100ms   Consumer               Poll & receive event              50ms
        │
101ms   Consumer               Deserialize JSON                  1ms
        │
102ms   Processor              Begin aggregation                 1ms
        │
105ms   Repository             Save event to PostgreSQL          3ms
        │
110ms   Domain Service         Create 4 aggregations             5ms
        │
        ├─ AD ─────────────────────────────────┐
        ├─ Campaign ───────────────────────────┤
        ├─ Country ────────────────────────────┤
        └─ Device ─────────────────────────────┘
                                               │
150ms   Cache Service          Check Redis (4 lookups)           40ms
        │
180ms   Repository             Update/Save aggregations          30ms
        │
190ms   Cache Service          Update Redis cache                10ms
        │
200ms   Consumer               Acknowledge message               10ms
        │
        ──────────────────────────────────────────────────────────
        TOTAL END-TO-END PROCESSING: ~200ms
```

---

## 4. Read Flow - Analytics Query

```
┌──────────┐
│  Client  │
└────┬─────┘
     │
     │ GET /api/v1/aggregations?dimension=AD&startTime=...&endTime=...
     │
     ▼
┌────────────────────────────────────────┐
│  AggregationController                 │
│                                        │
│  1. Parse query parameters             │
│  2. Validate time range                │
│  3. Create AggregationQuery object     │
└────┬───────────────────────────────────┘
     │
     │ AggregationQuery
     │
     ▼
┌────────────────────────────────────────┐
│  QueryAggregationUseCase               │
│                                        │
│  1. Validate query (isValid())         │
│  2. Build cache key                    │
│  3. Check cache first                  │
└────┬───────────────────────────────────┘
     │
     │ buildCacheKey(query)
     │ → "aggregation:query:AD:1640000000:1640086400"
     │
     ▼
┌────────────────────────────────────────┐
│  CacheService (Redis)                  │
│                                        │
│  get(cacheKey, List.class)             │
└────┬───────────────────────────────────┘
     │
     ├──── Cache Hit ─────┐
     │                    │
     │                    ▼
     │            ┌───────────────┐
     │            │ Return Cached │
     │            │ Results       │
     │            │ (TTL: 5 min)  │
     │            └───────┬───────┘
     │                    │
     │                    └────────────┐
     │                                 │
     └──── Cache Miss ────┐            │
                          │            │
                          ▼            │
        ┌────────────────────────────┐ │
        │  AggregationRepository     │ │
        │                            │ │
        │  1. Build SQL query        │ │
        │  2. Use indexes:           │ │
        │     - dimension            │ │
        │     - time_bucket          │ │
        │  3. Apply filters          │ │
        │  4. Order & limit results  │ │
        └────┬───────────────────────┘ │
             │                         │
             ▼                         │
        ┌────────────────────┐         │
        │  PostgreSQL        │         │
        │                    │         │
        │  SELECT *          │         │
        │  FROM aggregations │         │
        │  WHERE dimension=? │         │
        │    AND time_bucket │         │
        │    BETWEEN ? AND ? │         │
        │  ORDER BY          │         │
        │    click_count DESC│         │
        │  LIMIT ?           │         │
        └────┬───────────────┘         │
             │                         │
             │ List<AggregationEntity> │
             │                         │
             ▼                         │
        ┌────────────────────┐         │
        │  Entity Mapper     │         │
        │                    │         │
        │  toDomain(entities)│         │
        └────┬───────────────┘         │
             │                         │
             │ List<ClickAggregation>  │
             │                         │
             ▼                         │
        ┌────────────────────┐         │
        │  CacheService      │         │
        │                    │         │
        │  put(key, results, │         │
        │      TTL=5min)     │         │
        └────┬───────────────┘         │
             │                         │
             └─────────────────────────┘
                          │
                          ▼
        ┌────────────────────────────────┐
        │  AggregationMapper             │
        │                                │
        │  toResponseList(aggregations)  │
        └────┬───────────────────────────┘
             │
             │ List<AggregationResponse>
             │
             ▼
        ┌────────────────────┐
        │  HTTP Response     │
        │                    │
        │  Status: 200 OK    │
        │  Content-Type:     │
        │    application/json│
        │                    │
        │  [                 │
        │    {               │
        │      dimension:AD  │
        │      clickCount:123│
        │      totalCost:45.6│
        │      ...           │
        │    }               │
        │  ]                 │
        └────────────────────┘
```

### 4.1 Query Performance Path

```
Scenario 1: CACHE HIT (Best Case)
──────────────────────────────────
Client → Controller → UseCase → Redis → Response
         1ms          2ms       2ms      1ms

TOTAL: ~6ms


Scenario 2: CACHE MISS (Cold Query)
───────────────────────────────────
Client → Controller → UseCase → Redis (miss)
         1ms          2ms       2ms
                              ↓
                         PostgreSQL (indexed query)
                              5ms
                              ↓
                         Update Cache
                              2ms
                              ↓
                         Response
                              1ms

TOTAL: ~13ms


Scenario 3: COMPLEX QUERY (Multiple Dimensions)
───────────────────────────────────────────────
Client → Controller → UseCase → Redis (miss)
         1ms          2ms       2ms
                              ↓
                         PostgreSQL (join/aggregation)
                              15ms
                              ↓
                         Update Cache
                              3ms
                              ↓
                         Response
                              1ms

TOTAL: ~24ms
```

---

## 5. Async Processing Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ASYNC EVENT PROCESSING                           │
└─────────────────────────────────────────────────────────────────────┘

PRODUCER SIDE
─────────────

┌──────────────┐
│ REST API     │
│ processClick │
└──────┬───────┘
       │
       │ ClickEvent
       │
       ▼
┌──────────────────────────────┐
│ KafkaEventPublisher          │
│                              │
│ 1. Serialize to JSON         │
│ 2. Extract partition key     │
│    (campaign_id)             │
│ 3. Send async                │
└──────┬───────────────────────┘
       │
       │ CompletableFuture<SendResult>
       │
       ▼
┌──────────────────────────────────────────┐
│ Kafka Producer (Configuration)          │
│                                          │
│ - Batch Size: 32KB                       │
│ - Linger: 10ms                           │
│ - Compression: Snappy                    │
│ - Acks: 1 (leader only)                  │
│ - Buffer: 64MB                           │
│                                          │
│ Batching Strategy:                       │
│ ┌────────────────────────────┐          │
│ │ Wait 10ms OR 32KB batch    │          │
│ │ ├─ Event 1                 │          │
│ │ ├─ Event 2                 │          │
│ │ ├─ Event 3                 │          │
│ │ └─ ... (up to batch size)  │          │
│ └────────────────────────────┘          │
│         │                                │
│         │ Compress with Snappy           │
│         ▼                                │
│  ┌──────────────┐                       │
│  │ Compressed   │  Send to broker       │
│  │ Batch        │────────────────────►  │
│  └──────────────┘                       │
└──────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────┐
│ Kafka Broker Cluster                               │
│                                                    │
│  Topic: click-events                               │
│  Partitions: 3                                     │
│  Replication: 1                                    │
│                                                    │
│  Partition Assignment by campaign_id hash:         │
│                                                    │
│  ┌────────────────┐ ┌────────────────┐ ┌─────────┐│
│  │ Partition 0    │ │ Partition 1    │ │ Part. 2 ││
│  │                │ │                │ │         ││
│  │ campaign-001   │ │ campaign-002   │ │campaign ││
│  │ campaign-004   │ │ campaign-005   │ │ -003    ││
│  │ campaign-007   │ │ campaign-008   │ │campaign ││
│  │ ...            │ │ ...            │ │ -006    ││
│  │                │ │                │ │ ...     ││
│  │ Offset: 12,543 │ │ Offset: 11,234 │ │Offset:  ││
│  │                │ │                │ │ 13,456  ││
│  └────────────────┘ └────────────────┘ └─────────┘│
└────────────────────────────────────────────────────┘
                                │
                                │
CONSUMER SIDE                   │
─────────────                   │
                                ▼
┌────────────────────────────────────────────────────┐
│ Consumer Group: click-aggregator-consumer          │
│                                                    │
│ ┌────────────────┐ ┌────────────────┐ ┌─────────┐ │
│ │ Consumer 1     │ │ Consumer 2     │ │Consumer │ │
│ │ (Thread)       │ │ (Thread)       │ │   3     │ │
│ │                │ │                │ │         │ │
│ │ Reads:         │ │ Reads:         │ │ Reads:  │ │
│ │ Partition 0    │ │ Partition 1    │ │ Part. 2 │ │
│ │                │ │                │ │         │ │
│ │ Max Poll: 500  │ │ Max Poll: 500  │ │Max Poll │ │
│ │ records        │ │ records        │ │  500    │ │
│ └────────┬───────┘ └────────┬───────┘ └────┬────┘ │
│          │                  │               │      │
└──────────┼──────────────────┼───────────────┼──────┘
           │                  │               │
           └──────────┬───────┴───────────────┘
                      │
                      │ For each message
                      │
                      ▼
        ┌──────────────────────────────┐
        │ ClickEventConsumer           │
        │                              │
        │ 1. Deserialize JSON          │
        │ 2. Validate event            │
        │ 3. Process event             │
        │ 4. Acknowledge manually      │
        └──────┬───────────────────────┘
               │
               │ Try-Catch Block
               │
               ├─── Success ───┐
               │                │
               │                ▼
               │        ┌──────────────────┐
               │        │ Process Event    │
               │        │ acknowledgment   │
               │        │ .acknowledge()   │
               │        └──────────────────┘
               │
               └─── Error ────┐
                              │
                              ▼
                      ┌──────────────────┐
                      │ Error Handling   │
                      │                  │
                      │ - Log error      │
                      │ - Skip event     │
                      │ - (Future: DLQ)  │
                      └──────────────────┘
```

### 5.1 Kafka Consumer Rebalancing

```
Initial State (3 Partitions, 3 Consumers)
──────────────────────────────────────────

Consumer 1 ──► Partition 0
Consumer 2 ──► Partition 1
Consumer 3 ──► Partition 2


Consumer 2 Fails
────────────────

Consumer 1 ──► Partition 0
Consumer 2 ──► [DEAD]
Consumer 3 ──► Partition 2

    ↓ Rebalancing ↓

Consumer 1 ──► Partition 0, Partition 1
Consumer 3 ──► Partition 2


New Consumer Joins
──────────────────

Consumer 1 ──► Partition 0, Partition 1
Consumer 3 ──► Partition 2
Consumer 4 ──► [NEW]

    ↓ Rebalancing ↓

Consumer 1 ──► Partition 0
Consumer 3 ──► Partition 1
Consumer 4 ──► Partition 2
```

---

## 6. Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                    COMPONENT INTERACTIONS                           │
└─────────────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │  Client  │
    └────┬─────┘
         │
         │ [1] POST /clicks
         ▼
    ┌────────────────────┐
    │ClickEventController│
    └────┬───────────────┘
         │
         │ [2] toDomain()
         ▼
    ┌────────────────────┐
    │ClickEventMapper    │
    └────┬───────────────┘
         │
         │ [3] processClick(event)
         ▼
    ┌─────────────────────────┐
    │ProcessClickEventUseCase │
    └────┬────────────────────┘
         │
         ├─────────────────────┐
         │                     │
         │ [4a] Validate       │ [4b] Publish
         ▼                     ▼
    ┌──────────┐         ┌─────────────┐
    │ClickEvent│         │EventPublisher│
    │.isValid()│         │(Kafka)       │
    └──────────┘         └─────┬───────┘
                               │
                               │ [5] Send to topic
                               ▼
                         ┌───────────┐
                         │   Kafka   │
                         │  Broker   │
                         └─────┬─────┘
                               │
                               │ [6] Poll
                               ▼
                         ┌─────────────────┐
                         │ClickEventConsumer│
                         └─────┬───────────┘
                               │
                               │ [7] processClickEvent()
                               ▼
                         ┌───────────────────────┐
                         │ AggregationProcessor  │
                         └─────┬─────────────────┘
                               │
                               ├────────┬────────┬────────┐
                               │        │        │        │
                               │ [8a]   │ [8b]   │ [8c]   │ [8d]
                               │ Save   │Create  │Check   │Update
                               │ Event  │Aggs    │Cache   │DB/Cache
                               │        │        │        │
                               ▼        ▼        ▼        ▼
                         ┌─────────────────────────────────┐
                         │     ┌──────┐  ┌──────┐  ┌────┐ │
                         │     │ Repo │  │Cache │  │DB  │ │
                         │     └──────┘  └──────┘  └────┘ │
                         └─────────────────────────────────┘

PARALLEL QUERY PATH
───────────────────

    ┌──────────┐
    │  Client  │
    └────┬─────┘
         │
         │ [1] GET /aggregations
         ▼
    ┌──────────────────────┐
    │AggregationController │
    └────┬─────────────────┘
         │
         │ [2] queryAggregations(query)
         ▼
    ┌──────────────────────────┐
    │QueryAggregationUseCase   │
    └────┬─────────────────────┘
         │
         │ [3] Check cache
         ▼
    ┌──────────────┐
    │ CacheService │
    │   (Redis)    │
    └────┬─────────┘
         │
         ├─── Hit ─────────────┐
         │                     │
         └─── Miss             │
              │                │
              │ [4] Query DB   │
              ▼                │
         ┌────────────────┐    │
         │AggregationRepo │    │
         └────┬───────────┘    │
              │                │
              │ [5] Update     │
              │     Cache      │
              ▼                │
         ┌─────────┐           │
         │  Redis  │           │
         └─────────┘           │
              │                │
              └────────────────┘
                     │
                     │ [6] Map to DTO
                     ▼
              ┌──────────────┐
              │ Aggregation  │
              │   Mapper     │
              └──────┬───────┘
                     │
                     │ [7] Response
                     ▼
              ┌──────────────┐
              │   Client     │
              └──────────────┘
```

---

## 7. Deployment Architecture

### 7.1 Local Development

```
┌─────────────────────────────────────────────────────────────────┐
│                      Local Machine (Docker)                      │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Docker Network: click-network                             │ │
│  │                                                            │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐ │ │
│  │  │ PostgreSQL   │  │    Redis     │  │   Zookeeper     │ │ │
│  │  │ Container    │  │  Container   │  │   Container     │ │ │
│  │  │              │  │              │  │                 │ │ │
│  │  │ Port: 5432   │  │ Port: 6379   │  │  Port: 2181     │ │ │
│  │  │              │  │              │  │                 │ │ │
│  │  │ Volume:      │  │ Volume:      │  │                 │ │ │
│  │  │ postgres_data│  │ redis_data   │  │                 │ │ │
│  │  └──────────────┘  └──────────────┘  └─────────────────┘ │ │
│  │                                                            │ │
│  │  ┌──────────────────────┐  ┌──────────────────────────┐  │ │
│  │  │     Kafka Broker     │  │   Application           │  │ │
│  │  │     Container        │  │   Container             │  │ │
│  │  │                      │  │                          │  │ │
│  │  │ Port: 9092 (ext)     │  │ Port: 8080 (API)        │  │ │
│  │  │ Port: 9093 (int)     │  │                          │  │ │
│  │  │                      │  │ ┌────────────────────┐  │  │ │
│  │  │ Partitions: 3        │  │ │  Spring Boot App   │  │  │ │
│  │  │ Replication: 1       │  │ │                    │  │  │ │
│  │  └──────────────────────┘  │ │  - REST API        │  │  │ │
│  │                             │ │  - Kafka Consumer  │  │  │ │
│  │                             │ │  - Business Logic  │  │  │ │
│  └─────────────────────────────│ └────────────────────┘  │  │ │
│                                └──────────────────────────┘  │ │
│                                                              │ │
└──────────────────────────────────────────────────────────────┘ │
                                                                 │
                                                                 │
                    ┌─────────────────────────────────────────┐  │
                    │ Access from Host Machine:               │  │
                    │                                         │  │
                    │ http://localhost:8080/api/v1/clicks    │  │
                    │ http://localhost:8080/swagger-ui.html  │  │
                    │ http://localhost:8080/actuator/health  │  │
                    └─────────────────────────────────────────┘  │
                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 7.2 Production Deployment

```
┌─────────────────────────────────────────────────────────────────────┐
│                        INTERNET / CDN                               │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 │ HTTPS (443)
                                 │
┌────────────────────────────────▼────────────────────────────────────┐
│                    Load Balancer (AWS ALB / NGINX)                   │
│                                                                      │
│  - SSL Termination                                                   │
│  - Health Check: /actuator/health                                    │
│  - Sticky Sessions: Disabled (stateless)                             │
│  - Algorithm: Round Robin                                            │
└────────────┬────────────┬────────────┬────────────┬─────────────────┘
             │            │            │            │
             │            │            │            │
    ┌────────▼───┐   ┌────▼──────┐  ┌─▼──────────┐ ┌▼────────────┐
    │ App Pod 1  │   │App Pod 2  │  │App Pod 3   │ │ App Pod N   │
    │            │   │           │  │            │ │             │
    │ Container  │   │ Container │  │ Container  │ │ Container   │
    │ - 2 CPU    │   │ - 2 CPU   │  │ - 2 CPU    │ │ - 2 CPU     │
    │ - 4GB RAM  │   │ - 4GB RAM │  │ - 4GB RAM  │ │ - 4GB RAM   │
    │            │   │           │  │            │ │             │
    │ Replicas:  │   │           │  │            │ │ Auto-       │
    │ 3 minimum  │   │           │  │            │ │ Scaling     │
    └─────┬──────┘   └─────┬─────┘  └──────┬─────┘ └──────┬──────┘
          │                │               │              │
          └────────────────┴───────────────┴──────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         │                 │                 │
┌────────▼────────┐ ┌──────▼──────┐ ┌────────▼────────┐
│ Kafka Cluster   │ │  PostgreSQL │ │  Redis Cluster  │
│                 │ │   Cluster   │ │                 │
│ ┌─────────────┐ │ │             │ │ ┌─────────────┐ │
│ │ Broker 1    │ │ │ ┌─────────┐ │ │ │ Master      │ │
│ │ Broker 2    │ │ │ │ Primary │ │ │ │ Replica 1   │ │
│ │ Broker 3    │ │ │ │         │ │ │ │ Replica 2   │ │
│ └─────────────┘ │ │ │ Read    │ │ │ └─────────────┘ │
│                 │ │ │ Replica │ │ │                 │
│ - 3 Brokers     │ │ │ 1       │ │ │ - Cluster Mode  │
│ - Partitions: 9 │ │ │         │ │ │ - Persistence   │
│ - Replication:3 │ │ │ Read    │ │ │ - Sentinel      │
│ - Retention:7d  │ │ │ Replica │ │ │                 │
│                 │ │ │ 2       │ │ │                 │
└─────────────────┘ │ └─────────┘ │ └─────────────────┘
                    │             │
                    │ - Multi-AZ  │
                    │ - Backups   │
                    │ - Indexes   │
                    └─────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    Monitoring & Observability                    │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  Prometheus  │  │   Grafana    │  │   ELK Stack          │  │
│  │              │  │              │  │                      │  │
│  │ - Scrape     │  │ - Dashboards │  │ - Elasticsearch      │  │
│  │   /actuator/ │  │ - Alerts     │  │ - Logstash           │  │
│  │   prometheus │  │ - Visualize  │  │ - Kibana             │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │  PagerDuty   │  │   DataDog    │                            │
│  │  (Alerts)    │  │   (APM)      │                            │
│  └──────────────┘  └──────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
```

### 7.3 Multi-Region Deployment

```
┌────────────────────────────────────────────────────────────────┐
│                    Global Load Balancer                         │
│                   (Route 53 / CloudFlare)                       │
│                                                                 │
│  - Geo-routing                                                  │
│  - Failover                                                     │
│  - Health checks                                                │
└───────────┬───────────────────────┬───────────────────────┬────┘
            │                       │                       │
            │ US-EAST               │ EU-WEST               │ APAC
            │                       │                       │
┌───────────▼────────┐  ┌───────────▼────────┐  ┌──────────▼─────┐
│   Region: US-EAST  │  │  Region: EU-WEST   │  │ Region: APAC   │
│                    │  │                    │  │                │
│ ┌────────────────┐ │  │ ┌────────────────┐ │  │ ┌────────────┐ │
│ │ App Cluster    │ │  │ │ App Cluster    │ │  │ │ App Cluster│ │
│ │ - 5 instances  │ │  │ │ - 5 instances  │ │  │ │ -5 instances│ │
│ └────────────────┘ │  │ └────────────────┘ │  │ └────────────┘ │
│                    │  │                    │  │                │
│ ┌────────────────┐ │  │ ┌────────────────┐ │  │ ┌────────────┐ │
│ │ Kafka Cluster  │ │  │ │ Kafka Cluster  │ │  │ │Kafka Cluster││
│ └────────────────┘ │  │ └────────────────┘ │  │ └────────────┘ │
│                    │  │                    │  │                │
│ ┌────────────────┐ │  │ ┌────────────────┐ │  │ ┌────────────┐ │
│ │ PostgreSQL     │ │  │ │ PostgreSQL     │ │  │ │ PostgreSQL │ │
│ │ (Primary)      │◄─┼──┼─┤ (Replica)      │◄─┼──┼─┤ (Replica)  │ │
│ └────────────────┘ │  │ └────────────────┘ │  │ └────────────┘ │
│                    │  │                    │  │                │
│ ┌────────────────┐ │  │ ┌────────────────┐ │  │ ┌────────────┐ │
│ │ Redis Cluster  │ │  │ │ Redis Cluster  │ │  │ │Redis Cluster││
│ └────────────────┘ │  │ └────────────────┘ │  │ └────────────┘ │
└────────────────────┘  └────────────────────┘  └────────────────┘
         │                       │                       │
         └───────────────────────┴───────────────────────┘
                                 │
                    Cross-Region Replication
```

---

## 8. Data Model Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE SCHEMA                            │
└─────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────┐
│        click_events                │
├────────────────────────────────────┤
│ PK  event_id         VARCHAR(100)  │
│     ad_id            VARCHAR(100)  │◄───┐
│     campaign_id      VARCHAR(100)  │◄─┐ │
│     user_id          VARCHAR(100)  │  │ │
│     ip_address       VARCHAR(50)   │  │ │
│     user_agent       VARCHAR(500)  │  │ │
│     country          VARCHAR(10)   │◄┐│ │
│     device_type      VARCHAR(50)   │◄││ │
│     timestamp        TIMESTAMP     │ ││ │
│     cost             DECIMAL        │ ││ │
│     created_at       TIMESTAMP     │ ││ │
├────────────────────────────────────┤ ││ │
│ Indexes:                           │ ││ │
│  - idx_campaign_timestamp          │ ││ │
│  - idx_ad_timestamp                │ ││ │
│  - idx_timestamp                   │ ││ │
└────────────────────────────────────┘ ││ │
                                       ││ │
         Aggregated By                 ││ │
         Dimension                     ││ │
                │                      ││ │
                ▼                      ││ │
┌────────────────────────────────────┐ ││ │
│      click_aggregations            │ ││ │
├────────────────────────────────────┤ ││ │
│ PK  aggregation_id   VARCHAR(200)  │ ││ │
│     dimension        ENUM           │ ││ │
│       - AD           ────────────────┘│ │
│       - CAMPAIGN     ──────────────────┘
│       - COUNTRY      ───────────────────┘
│       - DEVICE_TYPE  ────────────────────┘
│     dimension_value  VARCHAR(100)  │
│     time_bucket      TIMESTAMP     │
│     click_count      BIGINT        │
│     total_cost       DECIMAL       │
│     unique_users     BIGINT        │
│     last_updated     TIMESTAMP     │
│     created_at       TIMESTAMP     │
├────────────────────────────────────┤
│ Indexes:                           │
│  - idx_dimension_value_time        │
│  - idx_dimension_time              │
│  - idx_click_count (DESC)          │
│  - idx_total_cost (DESC)           │
└────────────────────────────────────┘

EXAMPLE DATA
────────────

click_events:
┌───────────┬────────┬──────────┬──────┬─────────────────────┬──────┐
│ event_id  │ ad_id  │campaign  │user  │ timestamp           │ cost │
├───────────┼────────┼──────────┼──────┼─────────────────────┼──────┤
│ evt-001   │ ad-01  │ camp-01  │usr-1 │ 2024-01-15 10:23:45 │ 0.50 │
│ evt-002   │ ad-01  │ camp-01  │usr-2 │ 2024-01-15 10:24:12 │ 0.50 │
│ evt-003   │ ad-02  │ camp-01  │usr-3 │ 2024-01-15 10:25:33 │ 0.75 │
└───────────┴────────┴──────────┴──────┴─────────────────────┴──────┘

                            ↓ Aggregation ↓

click_aggregations (dimension: AD):
┌──────────────┬──────────┬───────┬─────────────────────┬─────┬──────┐
│aggregation_id│dimension │ value │ time_bucket         │count│ cost │
├──────────────┼──────────┼───────┼─────────────────────┼─────┼──────┤
│AD:ad-01:...  │ AD       │ad-01  │ 2024-01-15 10:00:00 │  2  │ 1.00 │
│AD:ad-02:...  │ AD       │ad-02  │ 2024-01-15 10:00:00 │  1  │ 0.75 │
└──────────────┴──────────┴───────┴─────────────────────┴─────┴──────┘

click_aggregations (dimension: CAMPAIGN):
┌──────────────┬──────────┬────────┬─────────────────────┬─────┬──────┐
│aggregation_id│dimension │ value  │ time_bucket         │count│ cost │
├──────────────┼──────────┼────────┼─────────────────────┼─────┼──────┤
│CAMPAIGN:...  │ CAMPAIGN │camp-01 │ 2024-01-15 10:00:00 │  3  │ 1.75 │
└──────────────┴──────────┴────────┴─────────────────────┴─────┴──────┘
```

---

## 9. Kafka Partitioning Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│              KAFKA PARTITIONING BY CAMPAIGN_ID                  │
└─────────────────────────────────────────────────────────────────┘

Producer Side:
──────────────

┌─────────────────┐
│  Click Event    │
│                 │
│ campaign_id:    │
│ "campaign-123"  │
└────────┬────────┘
         │
         │ hash(campaign_id)
         ▼
    ┌─────────┐
    │  Hash   │
    │Function │
    └────┬────┘
         │
         │ hash % 3 (num partitions)
         │ → partition number
         │
    ┌────▼──────────────────────────┐
    │  Partition Assignment         │
    │                               │
    │  hash("campaign-001") % 3 = 0 │
    │  hash("campaign-002") % 3 = 1 │
    │  hash("campaign-003") % 3 = 2 │
    │  hash("campaign-004") % 3 = 0 │
    │  hash("campaign-005") % 3 = 1 │
    └───────────────────────────────┘
                │
                ▼
┌────────────────────────────────────────────────────────────────┐
│                  Kafka Topic: click-events                     │
│                                                                │
│ ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐ │
│ │ Partition 0    │  │ Partition 1    │  │  Partition 2     │ │
│ │                │  │                │  │                  │ │
│ │ campaign-001:  │  │ campaign-002:  │  │ campaign-003:    │ │
│ │  [evt-1]       │  │  [evt-4]       │  │  [evt-7]         │ │
│ │  [evt-2]       │  │  [evt-5]       │  │  [evt-8]         │ │
│ │  [evt-3]       │  │  [evt-6]       │  │  [evt-9]         │ │
│ │                │  │                │  │                  │ │
│ │ campaign-004:  │  │ campaign-005:  │  │ campaign-006:    │ │
│ │  [evt-10]      │  │  [evt-13]      │  │  [evt-16]        │ │
│ │  [evt-11]      │  │  [evt-14]      │  │  [evt-17]        │ │
│ │  [evt-12]      │  │  [evt-15]      │  │  [evt-18]        │ │
│ │                │  │                │  │                  │ │
│ └────────────────┘  └────────────────┘  └──────────────────┘ │
│         │                   │                    │            │
└─────────┼───────────────────┼────────────────────┼────────────┘
          │                   │                    │
          │                   │                    │
          ▼                   ▼                    ▼
    ┌──────────┐        ┌──────────┐        ┌──────────┐
    │Consumer 1│        │Consumer 2│        │Consumer 3│
    └──────────┘        └──────────┘        └──────────┘

Benefits:
─────────
✓ All events for same campaign go to same partition
✓ Maintains ordering within a campaign
✓ Enables parallel processing across campaigns
✓ Consumer can aggregate by campaign efficiently
✓ Load balanced across partitions
```

---

## 10. Caching Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                    MULTI-LEVEL CACHING                          │
└─────────────────────────────────────────────────────────────────┘

Level 1: Application-Level Cache (Future Enhancement)
──────────────────────────────────────────────────────

┌────────────────────────────────┐
│  In-Memory Cache (Caffeine)    │
│                                │
│  - Size: 1000 entries          │
│  - TTL: 1 minute               │
│  - Scope: Per instance         │
│  - Use: Hot aggregations       │
└────────────────────────────────┘
              │
              │ Cache Miss
              ▼

Level 2: Distributed Cache (Redis) - CURRENT
─────────────────────────────────────────────

┌────────────────────────────────────────────────────────────┐
│                    Redis Cache                             │
│                                                            │
│  Cache Key Structure:                                      │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ aggregation:query:{dimension}:{start}:{end}          │ │
│  │ aggregation:top:ads:{limit}                          │ │
│  │ aggregation:top:campaigns:{limit}                    │ │
│  │ agg:current:{aggregation_id}                         │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  TTL Strategy:                                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Query Results:     5 minutes                         │ │
│  │ Top Lists:         5 minutes                         │ │
│  │ Current Aggs:     10 minutes                         │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Eviction Policy: LRU (Least Recently Used)               │
│  Max Memory: 2GB                                           │
└────────────────────────────────────────────────────────────┘
              │
              │ Cache Miss
              ▼

Level 3: Database with Indexes
───────────────────────────────

┌────────────────────────────────────────────────────────────┐
│                    PostgreSQL                              │
│                                                            │
│  Optimized with Indexes:                                   │
│  - idx_dimension_value_time (Composite)                    │
│  - idx_click_count (DESC)                                  │
│  - idx_total_cost (DESC)                                   │
│                                                            │
│  Query Optimization:                                       │
│  - Index-only scans                                        │
│  - Covering indexes                                        │
│  - Partition by time (future)                              │
└────────────────────────────────────────────────────────────┘


CACHE FLOW DIAGRAM
──────────────────

Request: GET /aggregations?dimension=AD&start=...&end=...
                            │
                            ▼
                 ┌─────────────────────┐
                 │  Build Cache Key    │
                 │  "agg:query:AD:..." │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │   Check Redis       │
                 └──────────┬──────────┘
                            │
              ┌─────────────┴──────────────┐
              │                            │
         Cache HIT                    Cache MISS
              │                            │
              ▼                            ▼
     ┌───────────────┐         ┌────────────────────┐
     │ Return from   │         │  Query PostgreSQL  │
     │ Redis (2ms)   │         │  (10-20ms)         │
     └───────────────┘         └──────┬─────────────┘
                                      │
                                      ▼
                           ┌────────────────────┐
                           │  Store in Redis    │
                           │  with TTL (5 min)  │
                           └──────┬─────────────┘
                                  │
                                  ▼
                           ┌────────────────────┐
                           │  Return Result     │
                           └────────────────────┘


CACHE INVALIDATION STRATEGY
────────────────────────────

Strategy: TTL-Based (Time To Live)
───────────────────────────────────

┌─────────────────────────────────────────────────────────────┐
│  Write Event (New Click)                                    │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│  Update Aggregation in DB                                   │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│  Update Current Aggregation Cache                           │
│  Key: "agg:current:{aggregation_id}"                        │
│  TTL: 10 minutes                                            │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│  Query Cache Auto-Expires after TTL                         │
│  - Next query will fetch fresh data                         │
│  - Trade-off: Eventual consistency (max 5 min stale)        │
└─────────────────────────────────────────────────────────────┘


Alternative: Write-Through (Future Enhancement)
────────────────────────────────────────────────

┌─────────────────────────────────────────────────────────────┐
│  Write Event                                                │
└────────────┬────────────────────────────────────────────────┘
             │
             ├──────────────┬──────────────┐
             │              │              │
             ▼              ▼              ▼
     ┌──────────┐   ┌──────────┐   ┌─────────────┐
     │   DB     │   │  Redis   │   │ Invalidate  │
     │  Write   │   │  Update  │   │ Query Cache │
     └──────────┘   └──────────┘   └─────────────┘
                                           │
                        Ensures immediate consistency
```

---

## Summary

These diagrams illustrate:

1. **System Architecture**: Complete view of all components and their relationships
2. **Hexagonal Architecture**: Clean separation of domain, application, and infrastructure
3. **Write Flow**: Detailed path from click ingestion to storage
4. **Read Flow**: Query optimization with caching
5. **Async Processing**: Kafka event streaming pipeline
6. **Component Interactions**: How all pieces work together
7. **Deployment**: Local, production, and multi-region setups
8. **Data Model**: Database schema and relationships
9. **Kafka Partitioning**: Load distribution strategy
10. **Caching**: Multi-level caching for performance

The architecture is designed for:
- **Scalability**: Horizontal scaling at every layer
- **Performance**: Caching and async processing
- **Reliability**: Replication and failover
- **Maintainability**: Clean architecture principles
- **Observability**: Comprehensive monitoring

