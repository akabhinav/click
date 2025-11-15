#!/bin/bash

# Local development setup script

set -e

echo "======================================="
echo "Setting up Ad Click Aggregator"
echo "======================================="
echo ""

# Check for required tools
echo "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    echo "Error: Docker is required but not installed."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "Error: Docker Compose is required but not installed."
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "Error: Java 17+ is required but not installed."
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is required but not installed."
    exit 1
fi

echo "All prerequisites satisfied!"
echo ""

# Build the application
echo "Building application..."
mvn clean package -DskipTests
echo ""

# Start infrastructure services
echo "Starting infrastructure services (PostgreSQL, Redis, Kafka)..."
docker-compose up -d postgres redis zookeeper kafka
echo ""

# Wait for services to be healthy
echo "Waiting for services to be ready..."
sleep 20

# Check service health
echo "Checking service health..."
docker-compose ps
echo ""

# Create Kafka topic
echo "Creating Kafka topic..."
docker exec click-kafka kafka-topics \
    --create \
    --topic click-events \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists || true
echo ""

echo "======================================="
echo "Setup completed successfully!"
echo "======================================="
echo ""
echo "To start the application:"
echo "  mvn spring-boot:run"
echo ""
echo "Or use Docker Compose for full deployment:"
echo "  docker-compose up -d"
echo ""
echo "API will be available at: http://localhost:8080"
echo "API Docs: http://localhost:8080/swagger-ui.html"
echo "Metrics: http://localhost:8080/actuator/prometheus"
echo ""
