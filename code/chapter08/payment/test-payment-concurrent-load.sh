#!/bin/bash

# Comprehensive load test for all fault tolerance patterns
# Tests retry, circuit breaker, bulkhead, and async under concurrent load

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Check if bc is installed
if ! command -v bc &> /dev/null; then
    echo -e "${YELLOW}Installing bc...${NC}"
    sudo apt-get update && sudo apt-get install -y bc
fi

# Dynamically determine the base URL
if [ -n "$CODESPACE_NAME" ] && [ -n "$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN" ]; then
    BASE_URL="http://localhost:9080/payment/api"
    METRICS_URL="http://localhost:9080/metrics?scope=base"
    echo -e "${CYAN}Detected GitHub Codespaces environment${NC}"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    BASE_URL="https://9080-$GITPOD_HOST/payment/api"
    METRICS_URL="https://9080-$GITPOD_HOST/metrics?scope=base"
    echo -e "${CYAN}Detected Gitpod environment${NC}"
else
    BASE_URL="http://localhost:9080/payment/api"
    METRICS_URL="http://localhost:9080/metrics?scope=base"
    echo -e "${CYAN}Using local environment${NC}"
fi

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Payment Service Concurrent Load Test         ║${NC}"
echo -e "${BLUE}║  Testing All Fault Tolerance Patterns         ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

# Test configuration
NUM_AUTHORIZE_REQUESTS=20
NUM_HEALTH_REQUESTS=15
NUM_NOTIFY_REQUESTS=15

echo -e "${YELLOW}Load Test Configuration:${NC}"
echo "  • Authorization requests (Retry): $NUM_AUTHORIZE_REQUESTS"
echo "  • Health check requests (Circuit Breaker): $NUM_HEALTH_REQUESTS"
echo "  • Notification requests (Async + Bulkhead): $NUM_NOTIFY_REQUESTS"
echo ""

read -p "Press Enter to start the load test..."
echo ""

# Counters
declare -A success_count=( ["authorize"]=0 ["health"]=0 ["notify"]=0 )
declare -A failure_count=( ["authorize"]=0 ["health"]=0 ["notify"]=0 )
declare -A fallback_count=( ["authorize"]=0 ["health"]=0 ["notify"]=0 )

# Function to send authorization request
send_authorize_request() {
    local id=$1
    local amount=$((50 + RANDOM % 450))
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "${BASE_URL}/authorize?amount=${amount}" 2>/dev/null || echo "HTTP_STATUS:000")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        if echo "$body" | grep -q "fallback"; then
            echo -e "${YELLOW}[AUTH-$id] ⚡ Fallback${NC}"
            ((fallback_count["authorize"]++))
        else
            echo -e "${GREEN}[AUTH-$id] ✓ Success${NC}"
            ((success_count["authorize"]++))
        fi
    else
        echo -e "${RED}[AUTH-$id] ✗ Failed${NC}"
        ((failure_count["authorize"]++))
    fi
}

# Function to send health check request
send_health_request() {
    local id=$1
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X GET "${BASE_URL}/health/gateway" 2>/dev/null || echo "HTTP_STATUS:000")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        if echo "$body" | grep -q "healthy"; then
            echo -e "${GREEN}[HEALTH-$id] ✓ Healthy${NC}"
            ((success_count["health"]++))
        else
            echo -e "${YELLOW}[HEALTH-$id] ⚠ Degraded${NC}"
            ((fallback_count["health"]++))
        fi
    else
        echo -e "${RED}[HEALTH-$id] ✗ Failed${NC}"
        ((failure_count["health"]++))
    fi
}

# Function to send notification request
send_notify_request() {
    local id=$1
    local payment_id=$(printf "PAY-%05d" $id)
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "${BASE_URL}/notify/${payment_id}" 2>/dev/null || echo "HTTP_STATUS:000")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        if echo "$body" | grep -q "fallback"; then
            echo -e "${YELLOW}[NOTIFY-$id] ⚡ Fallback${NC}"
            ((fallback_count["notify"]++))
        else
            echo -e "${GREEN}[NOTIFY-$id] ✓ Queued${NC}"
            ((success_count["notify"]++))
        fi
    else
        if echo "$body" | grep -q "Bulkhead"; then
            echo -e "${RED}[NOTIFY-$id] ✗ Rejected (Bulkhead)${NC}"
        else
            echo -e "${RED}[NOTIFY-$id] ✗ Failed${NC}"
        fi
        ((failure_count["notify"]++))
    fi
}

# Phase 1: Concurrent authorization requests
echo -e "${BLUE}=== Phase 1: Authorization Load Test (Retry Pattern) ===${NC}"
echo -e "${CYAN}Sending $NUM_AUTHORIZE_REQUESTS concurrent authorization requests...${NC}"
echo ""

start_time=$(date +%s.%N)

for i in $(seq 1 $NUM_AUTHORIZE_REQUESTS); do
    send_authorize_request $i &
    sleep 0.05
done

wait

end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)
duration_formatted=$(printf "%.2f" $duration)

echo ""
echo -e "${PURPLE}Authorization Phase Complete: ${duration_formatted}s${NC}"
echo -e "${GREEN}✓ Success: ${success_count["authorize"]}${NC}"
echo -e "${YELLOW}⚡ Fallback: ${fallback_count["authorize"]}${NC}"
echo -e "${RED}✗ Failed: ${failure_count["authorize"]}${NC}"
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

sleep 2

# Phase 2: Health check requests (circuit breaker)
echo -e "${BLUE}=== Phase 2: Health Check Load Test (Circuit Breaker) ===${NC}"
echo -e "${CYAN}Sending $NUM_HEALTH_REQUESTS health check requests...${NC}"
echo ""

start_time=$(date +%s.%N)

for i in $(seq 1 $NUM_HEALTH_REQUESTS); do
    send_health_request $i &
    sleep 0.1
done

wait

end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)
duration_formatted=$(printf "%.2f" $duration)

echo ""
echo -e "${PURPLE}Health Check Phase Complete: ${duration_formatted}s${NC}"
echo -e "${GREEN}✓ Healthy: ${success_count["health"]}${NC}"
echo -e "${YELLOW}⚠ Degraded: ${fallback_count["health"]}${NC}"
echo -e "${RED}✗ Failed: ${failure_count["health"]}${NC}"
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

sleep 2

# Phase 3: Notification requests (bulkhead + async)
echo -e "${BLUE}=== Phase 3: Notification Load Test (Bulkhead + Async) ===${NC}"
echo -e "${CYAN}Sending $NUM_NOTIFY_REQUESTS concurrent notification requests...${NC}"
echo ""

start_time=$(date +%s.%N)

for i in $(seq 1 $NUM_NOTIFY_REQUESTS); do
    send_notify_request $i &
    sleep 0.05
done

wait

end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)
duration_formatted=$(printf "%.2f" $duration)

echo ""
echo -e "${PURPLE}Notification Phase Complete: ${duration_formatted}s${NC}"
echo -e "${GREEN}✓ Queued: ${success_count["notify"]}${NC}"
echo -e "${YELLOW}⚡ Fallback: ${fallback_count["notify"]}${NC}"
echo -e "${RED}✗ Rejected: ${failure_count["notify"]}${NC}"
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Summary
total_requests=$((NUM_AUTHORIZE_REQUESTS + NUM_HEALTH_REQUESTS + NUM_NOTIFY_REQUESTS))
total_success=$((success_count["authorize"] + success_count["health"] + success_count["notify"]))
total_fallback=$((fallback_count["authorize"] + fallback_count["health"] + fallback_count["notify"]))
total_failure=$((failure_count["authorize"] + failure_count["health"] + failure_count["notify"]))

echo -e "${BLUE}╔════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║              Load Test Summary                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}Total Requests: ${total_requests}${NC}"
echo -e "${GREEN}✓ Successful: ${total_success}${NC}"
echo -e "${YELLOW}⚡ Fallback: ${total_fallback}${NC}"
echo -e "${RED}✗ Failed: ${total_failure}${NC}"
echo ""

success_rate=$(echo "scale=2; ($total_success * 100) / $total_requests" | bc)
echo -e "${CYAN}Success Rate: ${success_rate}%${NC}"
echo ""

# Fetch and display metrics
echo -e "${BLUE}=== Fault Tolerance Metrics Summary ===${NC}"
echo ""

echo -e "${YELLOW}Retry Metrics:${NC}"
curl -s "$METRICS_URL" 2>/dev/null | grep "ft_authorizePayment_retry" | head -5

echo ""
echo -e "${YELLOW}Circuit Breaker Metrics:${NC}"
curl -s "$METRICS_URL" 2>/dev/null | grep "ft_checkGatewayHealth_circuitbreaker" | head -5

echo ""
echo -e "${YELLOW}Bulkhead Metrics:${NC}"
curl -s "$METRICS_URL" 2>/dev/null | grep "ft_sendPaymentNotification_bulkhead" | head -5

echo ""
echo -e "${GREEN}=== Load Test Complete ===${NC}"
echo ""
echo -e "${CYAN}To view all metrics:${NC}"
echo -e "${BLUE}curl $METRICS_URL | grep \"ft_\"${NC}"
echo ""
    
    echo -e "${YELLOW}[Request $id] Sending payment request for \$$amount...${NC}"
    
    # Send request and capture response
    response=$(curl -s -X POST "${PAYMENT_URL}?amount=${amount}")
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    # Check if response contains "success" or "failed"
    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}[Request $id] SUCCESS: Payment processed in ${duration}s${NC}"
        echo -e "${GREEN}[Request $id] Response: $response${NC}"
    elif echo "$response" | grep -q "failed"; then
        echo -e "${RED}[Request $id] FALLBACK: Service unavailable (took ${duration}s)${NC}"
        echo -e "${RED}[Request $id] Response: $response${NC}"
    else
        echo -e "${RED}[Request $id] ERROR: Unexpected response (took ${duration}s)${NC}"
        echo -e "${RED}[Request $id] Response: $response${NC}"
    fi
    
    # Return the duration for analysis
    echo "$duration"
}

# Run concurrent requests using GNU Parallel if available, or background processes if not
if command -v parallel > /dev/null; then
    echo -e "${PURPLE}Using GNU Parallel for concurrent requests${NC}"
    export -f send_request
    export PAYMENT_URL RED GREEN YELLOW BLUE PURPLE CYAN NC
    
    # Use predefined amounts instead of bc calculation
    amounts=("15.99" "24.50" "19.95" "32.75" "12.99" "22.50" "18.75" "29.99" "14.50" "27.25")
    for i in $(seq 1 $NUM_REQUESTS); do
        amount_index=$((i-1 % 10))
        amount=${amounts[$amount_index]}
        send_request $i $amount &
    done
else
    echo -e "${PURPLE}Running concurrent requests using background processes${NC}"
    # Store PIDs
    pids=()
    
    # Predefined amounts
    amounts=("15.99" "24.50" "19.95" "32.75" "12.99" "22.50" "18.75" "29.99" "14.50" "27.25")
    
    # Launch requests in background
    for i in $(seq 1 $NUM_REQUESTS); do
        # Get amount from predefined list
        amount_index=$((i-1 % 10))
        amount=${amounts[$amount_index]}
        send_request $i $amount &
        pids+=($!)
        
        # Control concurrency
        if [ ${#pids[@]} -ge $CONCURRENCY ]; then
            # Wait for one process to finish before starting more
            wait "${pids[0]}"
            pids=("${pids[@]:1}")
        fi
    done
    
    # Wait for remaining processes
    for pid in "${pids[@]}"; do
        wait $pid
    done
fi

echo ""
echo -e "${BLUE}=== Test Complete ===${NC}"
echo -e "${CYAN}Analyze the responses to verify:${NC}"
echo -e "${CYAN}1. Asynchronous processing (@Asynchronous)${NC}"
echo -e "${CYAN}2. Resource isolation (@Bulkhead)${NC}"
echo -e "${CYAN}3. Retry behavior on failures (@Retry)${NC}"
