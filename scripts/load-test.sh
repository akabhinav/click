#!/bin/bash

# Load testing script to generate millions of click events
# Usage: ./scripts/load-test.sh [num_events] [concurrency]

set -e

NUM_EVENTS=${1:-1000000}
CONCURRENCY=${2:-10}
API_URL="http://localhost:8080/api/v1/clicks"

echo "Starting load test..."
echo "Events to generate: $NUM_EVENTS"
echo "Concurrency: $CONCURRENCY"
echo "Target URL: $API_URL"
echo ""

# Create sample ad IDs and campaign IDs
ADS=("ad-001" "ad-002" "ad-003" "ad-004" "ad-005" "ad-006" "ad-007" "ad-008" "ad-009" "ad-010")
CAMPAIGNS=("campaign-001" "campaign-002" "campaign-003" "campaign-004" "campaign-005")
COUNTRIES=("US" "UK" "CA" "DE" "FR" "JP" "AU" "BR" "IN" "CN")
DEVICES=("mobile" "desktop" "tablet")

# Function to generate a single click event
generate_click() {
    local ad_id=${ADS[$RANDOM % ${#ADS[@]}]}
    local campaign_id=${CAMPAIGNS[$RANDOM % ${#CAMPAIGNS[@]}]}
    local country=${COUNTRIES[$RANDOM % ${#COUNTRIES[@]}]}
    local device=${DEVICES[$RANDOM % ${#DEVICES[@]}]}
    local cost=$(awk -v min=0.10 -v max=2.00 'BEGIN{srand(); print min+rand()*(max-min)}')

    cat <<EOF
{
  "ad_id": "$ad_id",
  "campaign_id": "$campaign_id",
  "user_id": "user-$RANDOM",
  "ip_address": "192.168.1.$((RANDOM % 255))",
  "user_agent": "Mozilla/5.0",
  "country": "$country",
  "device_type": "$device",
  "cost": $cost
}
EOF
}

# Function to send clicks
send_clicks() {
    local count=$1
    local batch_size=100
    local sent=0

    while [ $sent -lt $count ]; do
        generate_click | curl -s -X POST "$API_URL" \
            -H "Content-Type: application/json" \
            -d @- > /dev/null

        sent=$((sent + 1))

        if [ $((sent % 1000)) -eq 0 ]; then
            echo "Sent: $sent events"
        fi
    done
}

# Calculate events per worker
events_per_worker=$((NUM_EVENTS / CONCURRENCY))

echo "Starting $CONCURRENCY parallel workers..."
start_time=$(date +%s)

# Launch parallel workers
for i in $(seq 1 $CONCURRENCY); do
    send_clicks $events_per_worker &
done

# Wait for all workers to complete
wait

end_time=$(date +%s)
duration=$((end_time - start_time))
throughput=$((NUM_EVENTS / duration))

echo ""
echo "Load test completed!"
echo "Total events: $NUM_EVENTS"
echo "Duration: ${duration}s"
echo "Throughput: ${throughput} events/sec"
