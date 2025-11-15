# Quick Start Guide

Get the Ad Click Aggregator running in 5 minutes!

## Step 1: Prerequisites Check

```bash
# Check Java version (need 17+)
java -version

# Check Docker
docker --version
docker-compose --version

# Check Maven
mvn --version
```

## Step 2: Start Infrastructure

```bash
# Start PostgreSQL, Redis, and Kafka
docker-compose up -d postgres redis zookeeper kafka

# Wait for services to be ready (about 30 seconds)
sleep 30
```

## Step 3: Build and Run

```bash
# Build the application
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run
```

## Step 4: Test the System

Open a new terminal and run:

```bash
# Send a test click event
curl -X POST http://localhost:8080/api/v1/clicks \
  -H "Content-Type: application/json" \
  -d '{
    "ad_id": "ad-001",
    "campaign_id": "campaign-001",
    "user_id": "user-001",
    "country": "US",
    "device_type": "mobile",
    "cost": 0.50
  }'

# Wait a few seconds for processing
sleep 3

# Query aggregations
curl "http://localhost:8080/api/v1/aggregations/top/ads?limit=10" | jq '.'
```

## Step 5: Load Test (Optional)

```bash
# Generate 10,000 click events
./scripts/load-test.sh 10000 5
```

## Step 6: View Metrics

```bash
# View application health
curl http://localhost:8080/actuator/health | jq '.'

# View metrics
curl http://localhost:8080/actuator/metrics/clicks.processed
```

## Access Points

- **API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus

## Common Commands

```bash
# View application logs
docker-compose logs -f app

# Stop everything
docker-compose down

# Clean up volumes
docker-compose down -v

# Restart application only
docker-compose restart app
```

## Next Steps

- Read the full [README.md](README.md)
- Explore the API documentation at http://localhost:8080/swagger-ui.html
- Try the load testing script: `./scripts/load-test.sh`
- Run the comprehensive API tests: `./scripts/test-api.sh`

## Troubleshooting

**Port 8080 already in use?**
```bash
# Find what's using the port
lsof -i :8080

# Kill the process or change the port in application-local.yml
```

**Services not starting?**
```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs postgres
docker-compose logs kafka
```

**Application can't connect to Kafka?**
```bash
# Recreate Kafka
docker-compose restart kafka
sleep 20
```
