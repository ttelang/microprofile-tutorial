#!/bin/bash

# Test script for Payment Service Bulkhead functionality
# Tests the @Bulkhead annotation on the sendPaymentNotification method

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
    echo -e "${YELLOW}Installing bc for duration calculations...${NC}"
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
echo -e "${BLUE}=== Payment Notification Bulkhead Test ===${NC}"
echo -e "${CYAN}Endpoint: POST /notify/{paymentId}${NC}"
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

echo -e "${YELLOW}Bulkhead Configuration:${NC}"
echo "  • Maximum Concurrent Requests: 3"
echo "  • Waiting Queue Size: 2"
echo "  • Total Capacity: 5 (3 concurrent + 2 queued)"
echo "  • Asynchronous: Yes (@Asynchronous)"
echo "  • Processing delay: ~2 seconds per notification"
echo ""

echo -e "${CYAN}🔍 Expected Behavior:${NC}"
echo "  • First 3 requests: Execute immediately (concurrent slots)"
echo "  • Next 2 requests: Queue for execution (waiting queue)"
echo "  • 6th+ requests: ${RED}REJECTED${NC} - Bulkhead full"
echo ""

read -p "Press Enter to start the bulkhead test..."
echo ""

# Function to send async notification request
send_notification() {
    local id=$1
    local payment_id="PAY-$(printf "%05d" $id)"
    
    start_time=$(date +%s.%N)
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/notify/${payment_id}" 2>/dev/null || echo "HTTP_STATUS:000")
    end_time=$(date +%s.%N)
    
    duration=$(echo "$end_time - $start_time" | bc)
    duration_formatted=$(printf "%.3f" $duration)
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 202 ]; then
        if echo "$body" | grep -q "queued\|sent"; then
            echo -e "${GREEN}[Request $id] ✓ Accepted (${duration_formatted}s) - Response: $body${NC}"
        elif echo "$body" | grep -q "fallback"; then
            echo -e "${PURPLE}[Request $id] ⚡ Fallback (${duration_formatted}s) - Response: $body${NC}"
        else
            echo -e "${GREEN}[Request $id] ✓ Success (${duration_formatted}s) - Response: $body${NC}"
        fi
    elif echo "$body" | grep -q "BulkheadException\|Bulkhead"; then
        echo -e "${RED}[Request $id] ✗ REJECTED: Bulkhead full (${duration_formatted}s)${NC}"
    elif [ "$http_code" -eq 503 ]; then
        echo -e "${YELLOW}[Request $id] ⚠ Service Unavailable (${duration_formatted}s)${NC}"
    else
        echo -e "${PURPLE}[Request $id] ? Unknown (HTTP $http_code, ${duration_formatted}s): $body${NC}"
    fi
}

# Phase 1: Single request baseline
echo -e "${BLUE}=== Phase 1: Single Request (Baseline) ===${NC}"
echo -e "${CYAN}Establishing baseline performance...${NC}"
echo ""

send_notification 1
sleep 3

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Phase 2: Test bulkhead capacity
echo -e "${BLUE}=== Phase 2: Testing Bulkhead Capacity (10 concurrent requests) ===${NC}"
echo -e "${CYAN}Sending 10 concurrent requests...${NC}"
echo -e "${YELLOW}Expected:${NC}"
echo -e "  • Requests 1-3: ${GREEN}Execute immediately (concurrent slots)${NC}"
echo -e "  • Requests 4-5: ${CYAN}Queued (waiting queue)${NC}"
echo -e "  • Requests 6-10: ${RED}REJECTED (bulkhead full)${NC}"
echo ""

# Send 10 requests concurrently in background
for i in {1..10}; do
    send_notification $i &
    sleep 0.1  # Small delay to stagger requests slightly
done

# Wait for all background jobs to complete
wait

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Phase 3: Check metrics
echo -e "${BLUE}=== Phase 3: Bulkhead Metrics ===${NC}"
echo -e "${CYAN}Fetching fault tolerance metrics...${NC}"
echo ""

sleep 1
metrics=$(curl -s "$METRICS_URL" 2>/dev/null | grep "ft_sendPaymentNotification_bulkhead")

if [ -n "$metrics" ]; then
    echo "$metrics" | while IFS= read -r line; do
        if echo "$line" | grep -q "callsAccepted"; then
            echo -e "${GREEN}$line${NC}"
        elif echo "$line" | grep -q "callsRejected"; then
            echo -e "${RED}$line${NC}"
        elif echo "$line" | grep -q "executionDuration"; then
            echo -e "${CYAN}$line${NC}"
        else
            echo "$line"
        fi
    done
else
    echo -e "${YELLOW}No bulkhead metrics found${NC}"
    echo -e "${CYAN}Make sure the service is running and has processed some requests${NC}"
fi

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Phase 4: Verify recovery after wait
echo -e "${BLUE}=== Phase 4: Testing Recovery ===${NC}"
echo -e "${CYAN}Waiting 3 seconds for in-progress notifications to complete...${NC}"
sleep 3

echo ""
echo -e "${CYAN}Sending new requests to verify bulkhead has freed up...${NC}"
echo ""

for i in {11..13}; do
    send_notification $i
    sleep 0.5
done

echo ""
echo -e "${GREEN}=== Bulkhead Test Complete ===${NC}"
echo ""

echo -e "${CYAN}Summary:${NC}"
echo "  • Bulkhead limits concurrent async executions"
echo "  • Requests beyond capacity are rejected immediately"
echo "  • Protects system resources from exhaustion"
echo "  • Works with @Asynchronous for non-blocking behavior"
echo ""

echo -e "${CYAN}To view all bulkhead metrics:${NC}"
echo -e  "${BLUE}curl $METRICS_URL | grep bulkhead${NC}"
echo ""
echo ""

# Set up log monitoring
LOG_FILE="/workspaces/liberty-rest-app/payment/target/liberty/wlp/usr/servers/mpServer/logs/messages.log"

# Get initial log position for later analysis
if [ -f "$LOG_FILE" ]; then
    LOG_POSITION=$(wc -l < "$LOG_FILE")
    echo -e "${CYAN}Found server log file at: $LOG_FILE${NC}"
    echo -e "${CYAN}Current log position: $LOG_POSITION${NC}"
else
    LOG_POSITION=0
    echo -e "${YELLOW}Warning: Server log file not found at: $LOG_FILE${NC}"
    echo -e "${YELLOW}Will continue without log analysis${NC}"
fi

echo ""

# ====================================
# Bulkhead Configuration
# ====================================
echo -e "${BLUE}=== PaymentService Bulkhead Configuration ===${NC}"
echo -e "${YELLOW}Your PaymentService has these bulkhead settings:${NC}"
echo -e "${CYAN}• Maximum Concurrent Requests: 5${NC}"
echo -e "${CYAN}• Asynchronous Processing: Yes${NC}"
echo -e "${CYAN}• Retry on failure: Yes (3 retries)${NC}"
echo -e "${CYAN}• Processing delay: ~1.5 seconds per attempt${NC}"
echo ""

echo -e "${YELLOW}🔍 WHAT TO EXPECT WITH BULKHEAD:${NC}"
echo -e "${CYAN}• Only 5 concurrent requests will be processed at a time${NC}"
echo -e "${CYAN}• Additional requests beyond the limit will be rejected${NC}"
echo -e "${CYAN}• Rejected requests will receive a 'Bulkhead full' message${NC}"
echo -e "${CYAN}• Successfully queued requests complete in ~1.5-10 seconds${NC}"
echo ""

echo -e "${YELLOW}Make sure the Payment Service is running on port 9080${NC}"
echo -e "${YELLOW}You can start it with: cd payment && mvn liberty:run${NC}"
echo ""
read -p "Press Enter to continue..." 
echo ""

# ====================================
# Test 1: Single Request (Baseline)
# ====================================
echo -e "${BLUE}=== Test 1: Single Request (Baseline) ===${NC}"
echo -e "${YELLOW}This test sends a single request to establish baseline performance${NC}"
echo ""

# Function to send a request and measure time
send_request() {
    local id=$1
    local amount=$2
    local start_time=$(date +%s.%N)
    
    response=$(curl -s -X POST "${BASE_URL}/authorize?amount=${amount}")
    status=$?
    
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc)
    duration=$(printf "%.2f" $duration)
    
    if [ $status -eq 0 ]; then
        if echo "$response" | grep -q "success"; then
            echo -e "${GREEN}[Request $id] SUCCESS: Payment processed in ${duration}s${NC}"
            echo -e "${GREEN}[Request $id] Response: $response${NC}"
        elif echo "$response" | grep -q "Bulkhead"; then
            echo -e "${YELLOW}[Request $id] REJECTED: Bulkhead full (took ${duration}s)${NC}"
            echo -e "${YELLOW}[Request $id] Response: $response${NC}"
        elif echo "$response" | grep -q "fallback"; then
            echo -e "${PURPLE}[Request $id] FALLBACK: Service used fallback (took ${duration}s)${NC}"
            echo -e "${PURPLE}[Request $id] Response: $response${NC}"
        else
            echo -e "${RED}[Request $id] ERROR: Unexpected response (took ${duration}s)${NC}"
            echo -e "${RED}[Request $id] Response: $response${NC}"
        fi
    else
        echo -e "${RED}[Request $id] ERROR: Failed to connect (took ${duration}s)${NC}"
    fi
    
    echo "$duration"
}

# Send a single request
echo -e "${CYAN}Sending single baseline request...${NC}"
send_request "Baseline" "50.00"

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# ====================================
# Test 2: Bulkhead Testing (10 concurrent requests)
# ====================================
echo -e "${BLUE}=== Test 2: Bulkhead Testing (10 concurrent requests) ===${NC}"
echo -e "${YELLOW}This test sends 10 concurrent requests to test bulkhead limits (5)${NC}"
echo -e "${YELLOW}Expected: 5 should be processed, others should be rejected or queued${NC}"
echo ""

# Function to generate a random amount between 10 and 200
random_amount() {
    echo "$(( (RANDOM % 190) + 10 )).99"
}

# Send concurrent requests to test bulkhead
echo -e "${CYAN}Sending 10 concurrent requests...${NC}"
pids=()
results=()

for i in {1..10}; do
    amount=$(random_amount)
    echo -e "${PURPLE}[Request $i/10] Initiating payment request for \$$amount...${NC}"
    send_request "$i" "$amount" > /tmp/bulkhead_result_$i.txt &
    pids+=($!)
done

# Wait for all requests to complete
for pid in "${pids[@]}"; do
    wait $pid
done

# Collect results
echo ""
echo -e "${BLUE}=== Results Summary ===${NC}"
success_count=0
rejected_count=0
fallback_count=0
error_count=0

for i in {1..10}; do
    result=$(cat /tmp/bulkhead_result_$i.txt)
    rm /tmp/bulkhead_result_$i.txt
    results+=($result)
    
    # Count results by parsing the output log
    log_entry=$(grep "\[Request $i\]" /tmp/bulkhead.log 2>/dev/null || echo "")
    if echo "$log_entry" | grep -q "SUCCESS"; then
        success_count=$((success_count + 1))
    elif echo "$log_entry" | grep -q "REJECTED"; then
        rejected_count=$((rejected_count + 1))
    elif echo "$log_entry" | grep -q "FALLBACK"; then
        fallback_count=$((fallback_count + 1))
    else
        error_count=$((error_count + 1))
    fi
done

echo -e "${CYAN}Successful requests: $success_count${NC}"
echo -e "${CYAN}Rejected requests: $rejected_count${NC}"
echo -e "${CYAN}Fallback requests: $fallback_count${NC}"
echo -e "${CYAN}Error requests: $error_count${NC}"

# ====================================
# Log Analysis
# ====================================
if [ -f "$LOG_FILE" ] && [ $LOG_POSITION -gt 0 ]; then
    echo ""
    echo -e "${BLUE}=== Server Log Analysis ===${NC}"
    
    # Extract relevant log entries
    echo -e "${CYAN}Latest log entries related to payment processing:${NC}"
    tail -n +$LOG_POSITION "$LOG_FILE" | grep -E "Processing payment for amount|Bulkhead|concurrent|reject" | head -20
fi

# ====================================
# Test 3: Sequential Requests (After Bulkhead Test)
# ====================================
echo ""
echo -e "${BLUE}=== Test 3: Sequential Requests After Bulkhead Test ===${NC}"
echo -e "${YELLOW}This test sends 3 sequential requests to verify service recovery${NC}"
echo -e "${YELLOW}Expected: All should succeed now that the bulkhead pressure is released${NC}"
echo ""

# Wait a moment for bulkhead to clear
echo -e "${CYAN}Waiting 5 seconds for bulkhead to clear...${NC}"
sleep 5

# Send 3 sequential requests
for i in {1..3}; do
    amount=$(random_amount)
    echo -e "${CYAN}Sending sequential request $i/3 (\$$amount)...${NC}"
    send_request "Sequential-$i" "$amount"
    echo ""
    sleep 1
done

# ====================================
# Summary and Conclusion
# ====================================
echo ""
echo -e "${BLUE}==============================================${NC}"
echo -e "${BLUE}     Bulkhead Testing Summary     ${NC}"
echo -e "${BLUE}==============================================${NC}"
echo ""

echo -e "${GREEN}=== Bulkhead Testing Complete ===${NC}"
echo ""
echo -e "${YELLOW}Key observations:${NC}"
echo -e "${CYAN}1. The PaymentService uses a bulkhead of 5 concurrent requests${NC}"
echo -e "${CYAN}2. Requests beyond the bulkhead limit are rejected with an error${NC}"
echo -e "${CYAN}3. Successful requests complete even under concurrent load${NC}"
echo -e "${CYAN}4. The service recovers quickly once load decreases${NC}"
echo -e "${CYAN}5. This protects the system from being overwhelmed${NC}"
echo ""
echo -e "${YELLOW}This demonstrates how the @Bulkhead annotation:${NC}"
echo -e "${CYAN}• Limits concurrent requests to prevent system overload${NC}"
echo -e "${CYAN}• Rejects excess requests instead of queuing them indefinitely${NC}"
echo -e "${CYAN}• Protects the system during traffic spikes${NC}"
echo -e "${CYAN}• Works with other fault tolerance annotations (@Retry, @Fallback, etc.)${NC}"
echo ""
echo -e "${YELLOW}For more details, see:${NC}"
echo -e "${CYAN}• MicroProfile Fault Tolerance Documentation${NC}"
echo -e "${CYAN}• https://download.eclipse.org/microprofile/microprofile-fault-tolerance-3.0/apidocs/org/eclipse/microprofile/faulttolerance/Bulkhead.html${NC}"
