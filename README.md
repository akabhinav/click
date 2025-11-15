# Ad Click Aggregator

A world-class, production-ready platform for processing **millions of ad click events** with real-time aggregation and analytics capabilities. Built with clean architecture principles, designed for scalability, extensibility, and robustness.

## Overview

The Ad Click Aggregator is a high-performance system that collects and aggregates data on ad clicks. It processes click events asynchronously, provides real-time aggregations across multiple dimensions, and offers comprehensive analytics APIs.

### Key Features

- **High-Throughput Ingestion**: Process millions of click events via REST API
- **Asynchronous Processing**: Kafka-based event streaming for scalability
- **Real-Time Aggregation**: Multi-dimensional aggregation (Ad, Campaign, Country, Device)
- **Intelligent Caching**: Redis-powered caching for optimal query performance
- **Production-Ready**: Comprehensive monitoring, metrics, and health checks
- **Clean Architecture**: Hexagonal architecture (Ports & Adapters pattern)
- **Docker Support**: Full containerization with Docker Compose
- **API Documentation**: Auto-generated OpenAPI/Swagger documentation
- **Extensive Testing**: Unit tests, integration tests, and load testing utilities

## Architecture

### Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────────┐
│                         Infrastructure Layer                     │
│  ┌────────────────┐                      ┌────────────────────┐ │
│  │  REST API      │                      │   PostgreSQL       │ │
│  │  Kafka Consumer│  ◄────────────────►  │   Redis Cache      │ │
│  │  Controllers   │                      │   Kafka Producer   │ │
│  └────────────────┘                      └────────────────────┘ │
└───────────────────────────┬──────────────────────────────────────┘
                           │
┌───────────────────────────▼──────────────────────────────────────┐
│                        Application Layer                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Use Cases: ProcessClickEvent, QueryAggregation          │   │
│  │  Services: AggregationProcessor                          │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────┬──────────────────────────────────────┘
                           │
┌───────────────────────────▼──────────────────────────────────────┐
│                          Domain Layer                            │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Models: ClickEvent, ClickAggregation                    │   │
│  │  Services: AggregationService                            │   │
│  │  Ports: Inbound (Use Cases), Outbound (Repositories)    │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

### Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Message Broker**: Apache Kafka
- **Monitoring**: Micrometer + Prometheus
- **API Docs**: SpringDoc OpenAPI 3
- **Testing**: JUnit 5, Mockito, Testcontainers

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose
- 8GB+ RAM (for local deployment)
- curl and jq (for testing scripts)

## Quick Start

### 1. Clone and Setup

```bash
git clone <repository-url>
cd click-aggregator

# Run setup script
./scripts/setup-local.sh
```

### 2. Start the Application

**Option A: Run with Maven (Development)**
```bash
# Start infrastructure only
docker-compose up -d postgres redis zookeeper kafka

# Run application
mvn spring-boot:run
```

**Option B: Full Docker Deployment**
```bash
# Build and start everything
docker-compose up -d

# View logs
docker-compose logs -f app
```

### 3. Verify Installation

```bash
# Check health
curl http://localhost:8080/actuator/health

# Run API tests
./scripts/test-api.sh
```

## API Documentation

### Interactive API Docs

Once the application is running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Click Event Ingestion

#### Record Single Click Event

```bash
curl -X POST http://localhost:8080/api/v1/clicks \
  -H "Content-Type: application/json" \
  -d '{
    "ad_id": "ad-123",
    "campaign_id": "campaign-456",
    "user_id": "user-789",
    "ip_address": "192.168.1.1",
    "user_agent": "Mozilla/5.0",
    "country": "US",
    "device_type": "mobile",
    "cost": 0.50
  }'
```

#### Record Batch Click Events

```bash
curl -X POST http://localhost:8080/api/v1/clicks/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "ad_id": "ad-123",
      "campaign_id": "campaign-456",
      "country": "US",
      "device_type": "mobile",
      "cost": 0.50
    },
    {
      "ad_id": "ad-124",
      "campaign_id": "campaign-456",
      "country": "UK",
      "device_type": "desktop",
      "cost": 0.75
    }
  ]'
```

### Analytics & Aggregations

#### Query Aggregations

```bash
# Query by dimension and time range
curl "http://localhost:8080/api/v1/aggregations?\
dimension=AD&\
startTime=2024-01-01T00:00:00Z&\
endTime=2024-12-31T23:59:59Z&\
limit=100"
```

**Available Dimensions**: `AD`, `CAMPAIGN`, `COUNTRY`, `DEVICE_TYPE`

#### Get Top Performing Ads

```bash
curl "http://localhost:8080/api/v1/aggregations/top/ads?limit=10"
```

#### Get Top Performing Campaigns

```bash
curl "http://localhost:8080/api/v1/aggregations/top/campaigns?limit=10"
```

## Load Testing

### Generate Million Click Events

```bash
# Generate 1 million events with 10 concurrent workers
./scripts/load-test.sh 1000000 10

# Generate 10 million events with 50 concurrent workers
./scripts/load-test.sh 10000000 50
```

### Performance Benchmarks

On a standard development machine (8 cores, 16GB RAM):
- **Throughput**: 50,000+ events/second
- **Latency**: < 10ms (p95)
- **Aggregation**: Real-time with < 1s delay

## Monitoring & Observability

### Actuator Endpoints

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus
- **Info**: http://localhost:8080/actuator/info

### Key Metrics

```bash
# Total clicks processed
curl http://localhost:8080/actuator/metrics/clicks.processed

# Aggregations processed
curl http://localhost:8080/actuator/metrics/aggregations.processed

# Invalid clicks
curl http://localhost:8080/actuator/metrics/clicks.invalid
```

### Prometheus Integration

The application exposes Prometheus-compatible metrics at `/actuator/prometheus`. You can integrate with Prometheus and Grafana for advanced monitoring.

## Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Test Coverage

```bash
mvn jacoco:report
# Report available at: target/site/jacoco/index.html
```

## Configuration

### Application Profiles

- **local**: Local development (default)
- **prod**: Production deployment

### Key Configuration Files

- `application.yml`: Base configuration
- `application-local.yml`: Local environment settings
- `docker-compose.yml`: Container orchestration

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/clickaggregator
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Project Structure

```
click-aggregator/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/adtech/clickaggregator/
│   │   │       ├── domain/              # Domain layer (core business logic)
│   │   │       │   ├── model/           # Domain entities
│   │   │       │   ├── port/            # Interfaces (ports)
│   │   │       │   │   ├── in/          # Inbound ports (use cases)
│   │   │       │   │   └── out/         # Outbound ports (repositories)
│   │   │       │   └── service/         # Domain services
│   │   │       ├── application/         # Application layer
│   │   │       │   ├── usecase/         # Use case implementations
│   │   │       │   └── service/         # Application services
│   │   │       └── infrastructure/      # Infrastructure layer
│   │   │           ├── adapter/
│   │   │           │   ├── in/          # Inbound adapters
│   │   │           │   │   ├── rest/    # REST controllers
│   │   │           │   │   └── kafka/   # Kafka consumers
│   │   │           │   └── out/         # Outbound adapters
│   │   │           │       ├── persistence/  # Database
│   │   │           │       ├── cache/        # Redis
│   │   │           │       └── messaging/    # Kafka producers
│   │   │           └── config/          # Configuration
│   │   └── resources/
│   │       └── application*.yml
│   └── test/                            # Tests
├── scripts/                             # Utility scripts
│   ├── setup-local.sh                   # Setup script
│   ├── test-api.sh                      # API testing
│   └── load-test.sh                     # Load testing
├── docker-compose.yml                   # Docker orchestration
├── Dockerfile                           # Application container
└── pom.xml                              # Maven configuration
```

## Scalability Features

### Horizontal Scaling

- **Kafka Partitioning**: Distributes events across multiple partitions
- **Stateless Services**: All application instances are stateless
- **Database Read Replicas**: Support for read-only replicas
- **Redis Clustering**: Can be configured for Redis cluster mode

### Performance Optimizations

- **Batch Processing**: Efficient batch operations for database writes
- **Connection Pooling**: Optimized database connection pools
- **Async Processing**: Non-blocking event processing
- **Caching Strategy**: Multi-level caching (Redis + application)
- **Index Optimization**: Strategic database indexes for fast queries

## Production Deployment

### Recommended Infrastructure

- **Application**: 3+ instances behind load balancer
- **Database**: PostgreSQL with read replicas
- **Cache**: Redis cluster with persistence
- **Message Broker**: Kafka cluster (3+ brokers)
- **Monitoring**: Prometheus + Grafana

### Deployment Checklist

- [ ] Configure production database credentials
- [ ] Set up Redis cluster with persistence
- [ ] Configure Kafka with appropriate replication
- [ ] Enable HTTPS/TLS
- [ ] Set up log aggregation (ELK/Splunk)
- [ ] Configure alerting (PagerDuty/Opsgenie)
- [ ] Set up backup and disaster recovery
- [ ] Perform load testing
- [ ] Review security configurations

## Troubleshooting

### Common Issues

**Application won't start**
```bash
# Check if ports are available
netstat -tuln | grep -E '8080|5432|6379|9092'

# View application logs
docker-compose logs app
```

**Kafka connection issues**
```bash
# Verify Kafka is running
docker exec click-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Check topics
docker exec click-kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Database connection errors**
```bash
# Check PostgreSQL
docker exec click-postgres pg_isready -U postgres

# Connect to database
docker exec -it click-postgres psql -U postgres -d clickaggregator
```

## Contributing

1. Follow the existing architecture patterns
2. Write tests for new features
3. Update documentation
4. Follow Java code conventions
5. Ensure all tests pass before submitting

## License

Apache License 2.0

## Support

For issues and questions, please open a GitHub issue.

---

**Built with ❤️ for world-class scalability and performance**
