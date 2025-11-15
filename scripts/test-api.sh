#!/bin/bash

# API testing script
# Tests all endpoints of the Click Aggregator API

set -e

API_URL="http://localhost:8080/api/v1"

echo "======================================="
echo "Ad Click Aggregator API Tests"
echo "======================================="
echo ""

# Test 1: Health check
echo "Test 1: Health Check"
curl -s "$API_URL/clicks/health"
echo -e "\n"

# Test 2: Send a single click event
echo "Test 2: Send Single Click Event"
curl -X POST "$API_URL/clicks" \
  -H "Content-Type: application/json" \
  -d '{
    "ad_id": "ad-test-001",
    "campaign_id": "campaign-test-001",
    "user_id": "user-test-001",
    "ip_address": "192.168.1.100",
    "user_agent": "Mozilla/5.0",
    "country": "US",
    "device_type": "mobile",
    "cost": 0.50
  }' | jq '.'
echo -e "\n"

# Test 3: Send batch of click events
echo "Test 3: Send Batch Click Events"
curl -X POST "$API_URL/clicks/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "ad_id": "ad-test-001",
      "campaign_id": "campaign-test-001",
      "user_id": "user-test-002",
      "country": "US",
      "device_type": "mobile",
      "cost": 0.50
    },
    {
      "ad_id": "ad-test-002",
      "campaign_id": "campaign-test-001",
      "user_id": "user-test-003",
      "country": "UK",
      "device_type": "desktop",
      "cost": 0.75
    }
  ]' | jq '.'
echo -e "\n"

# Wait for processing
echo "Waiting for events to be processed..."
sleep 3

# Test 4: Query aggregations by AD
echo "Test 4: Query Aggregations (AD dimension)"
START_TIME=$(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%SZ)
END_TIME=$(date -u -d '1 hour' +%Y-%m-%dT%H:%M:%SZ)

curl -s "$API_URL/aggregations?dimension=AD&startTime=$START_TIME&endTime=$END_TIME&limit=10" | jq '.'
echo -e "\n"

# Test 5: Get top ads
echo "Test 5: Get Top Ads"
curl -s "$API_URL/aggregations/top/ads?limit=5" | jq '.'
echo -e "\n"

# Test 6: Get top campaigns
echo "Test 6: Get Top Campaigns"
curl -s "$API_URL/aggregations/top/campaigns?limit=5" | jq '.'
echo -e "\n"

# Test 7: Actuator health endpoint
echo "Test 7: Actuator Health"
curl -s "http://localhost:8080/actuator/health" | jq '.'
echo -e "\n"

# Test 8: Metrics endpoint
echo "Test 8: Prometheus Metrics (sample)"
curl -s "http://localhost:8080/actuator/prometheus" | grep "clicks_processed" | head -5
echo -e "\n"

echo "======================================="
echo "All tests completed!"
echo "======================================="
