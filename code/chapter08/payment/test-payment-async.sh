#!/bin/bash

# Test script for asynchronous payment notification processing
# Demonstrates @Asynchronous annotation behavior

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
    echo -e "${CYAN}Detected GitHub Codespaces environment${NC}"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    BASE_URL="https://9080-$GITPOD_HOST/payment/api"
    echo -e "${CYAN}Detected Gitpod environment${NC}"
else
    BASE_URL="http://localhost:9080/payment/api"
    echo -e "${CYAN}Using local environment${NC}"
fi

echo ""
echo -e "${BLUE}=== Testing Asynchronous Payment Notifications ===${NC}"
echo -e "${CYAN}Endpoint: POST /notify/{paymentId}${NC}"
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

echo -e "${YELLOW}Asynchronous Configuration:${NC}"
echo "  • Method: sendPaymentNotification()"
echo "  • Return Type: CompletionStage<String>"
echo "  • Processing: Non-blocking"
echo "  • Simulated delay: ~2 seconds"
echo ""

echo -e "${CYAN}🔍 Testing Goals:${NC}"
echo "  1. Verify requests return immediately (non-blocking)"
echo "  2. Demonstrate concurrent processing"
echo "  3. Show CompletionStage behavior"
echo ""

# Function to send notification and measure response time
send_async_notification() {
    local id=$1
    local payment_id=$2
    
    start_time=$(date +%s.%N)
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}\nTIME_TOTAL:%{time_total}" \
        -X POST "${BASE_URL}/notify/${payment_id}" 2>/dev/null || echo "HTTP_STATUS:000")
    end_time=$(date +%s.%N)
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    curl_time=$(echo "$response" | grep "TIME_TOTAL:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d' | sed '/TIME_TOTAL:/d')
    
    wall_time=$(echo "$end_time - $start_time" | bc)
    wall_time_formatted=$(printf "%.3f" $wall_time)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        # Check if response was truly async (should be fast, not 2 seconds)
        is_async=$(echo "$curl_time < 0.5" | bc)
        if [ "$is_async" -eq 1 ]; then
            echo -e "${GREEN}[Request $id] ✓ ASYNC - Returned immediately (${wall_time_formatted}s)${NC}"
            echo -e "${GREEN}            Response: $body${NC}"
        else
            echo -e "${YELLOW}[Request $id] ⚠ SYNC? - Took ${wall_time_formatted}s (expected < 0.5s)${NC}"
            echo -e "${YELLOW}            Response: $body${NC}"
        fi
    else
        echo -e "${RED}[Request $id] ✗ ERROR (HTTP $http_code, ${wall_time_formatted}s)${NC}"
        echo -e "${RED}            Response: $body${NC}"
    fi
    
    echo "$wall_time_formatted"
}

# Test 1: Single async request
echo -e "${BLUE}=== Test 1: Single Asynchronous Request ===${NC}"
echo -e "${CYAN}Sending single notification to verify async behavior...${NC}"
echo ""

send_async_notification 1 "PAY-00001"

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Test 2: Multiple sequential requests
echo -e "${BLUE}=== Test 2: Sequential Asynchronous Requests ===${NC}"
echo -e  "${CYAN}Sending 5 sequential requests...${NC}"
echo -e "${YELLOW}Each should return immediately (< 0.5s) despite 2s processing time${NC}"
echo ""

total_start=$(date +%s.%N)

for i in {2..6}; do
    payment_id=$(printf "PAY-%05d" $i)
    send_async_notification $i $payment_id
done

total_end=$(date +%s.%N)
total_time=$(echo "$total_end - $total_start" | bc)
total_time_formatted=$(printf "%.2f" $total_time)

echo ""
echo -e "${PURPLE}Total time for 5 sequential requests: ${total_time_formatted}s${NC}"
echo -e "${CYAN}Expected: < 2.5s if async, ~10s if blocking${NC}"

if (( $(echo "$total_time < 3" | bc -l) )); then
    echo -e "${GREEN}✓ CONFIRMED: Requests are truly asynchronous!${NC}"
else
    echo -e "${YELLOW}⚠ WARNING: Requests may be blocking (total time too high)${NC}"
fi

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Test 3: Concurrent requests
echo -e "${BLUE}=== Test 3: Concurrent Asynchronous Requests ===${NC}"
echo -e "${CYAN}Sending 5 concurrent requests in parallel...${NC}"
echo -e "${YELLOW}All should complete quickly, demonstrating true async processing${NC}"
echo ""

concurrent_start=$(date +%s.%N)

# Launch background jobs
for i in {7..11}; do
    payment_id=$(printf "PAY-%05d" $i)
    (send_async_notification $i $payment_id) &
    sleep 0.05  # Small stagger to make output readable
done

# Wait for all background jobs
wait

concurrent_end=$(date +%s.%N)
concurrent_time=$(echo "$concurrent_end - $concurrent_start" | bc)
concurrent_time_formatted=$(printf "%.2f" $concurrent_time)

echo ""
echo -e "${PURPLE}Total time for 5 concurrent requests: ${concurrent_time_formatted}s${NC}"
echo -e "${CYAN}Expected: ~0.5-1.0s (all start immediately)${NC}"

if (( $(echo "$concurrent_time < 2" | bc -l) )); then
    echo -e "${GREEN}✓ EXCELLENT: Concurrent async processing confirmed!${NC}"
else
    echo -e "${YELLOW}⚠ Note: Time higher than expected${NC}"
fi

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Summary
echo -e "${BLUE}=== Asynchronous Processing Summary ===${NC}"
echo ""
echo -e "${CYAN}Key Observations:${NC}"
echo "  • @Asynchronous methods return CompletionStage"
echo "  • JAX-RS endpoint returns CompletionStage<Response>"
echo "  • Client receives immediate response (non-blocking)"
echo "  • Server processes work in background thread pool"
echo "  • Multiple requests can execute concurrently"
echo ""

echo -e "${CYAN}CompletionStage vs Future:${NC}"
echo "  • CompletionStage: Fault tolerance applied, supports chaining"
echo "  • Future: Fault tolerance NOT applied (avoid!)"
echo ""

echo -e "${GREEN}=== Test Complete ===${NC}"
echo ""
