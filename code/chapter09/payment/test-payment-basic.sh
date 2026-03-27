#!/bin/bash

# Basic test script for payment service
# Tests all three fault-tolerant endpoints

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Check if bc is installed
if ! command -v bc &> /dev/null; then
    echo -e "${YELLOW}The 'bc' command is not found. Installing bc...${NC}"
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

echo -e "${BLUE}=== Payment Service Basic Test ===${NC}"
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

# Test 1: Payment Authorization
echo -e "${YELLOW}Test 1: Payment Authorization (Retry + Timeout + Fallback)${NC}"
echo -e "${CYAN}Endpoint: POST /authorize?amount=100${NC}"
echo ""

start_time=$(date +%s.%N)
response=$(curl -s -X POST "${BASE_URL}/authorize?amount=100")
end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)

echo -e "${GREEN}Response:${NC} $response"
echo -e "${GREEN}Duration:${NC} ${duration}s"
echo ""

if echo "$response" | grep -q "success"; then
    echo -e "${GREEN}✓ SUCCESS: Payment authorized${NC}"
elif echo "$response" | grep -q "fallback"; then
    echo -e "${YELLOW}⚠ FALLBACK: Used fallback response${NC}"
else
    echo -e "${RED}✗ ERROR: Unexpected response${NC}"
fi

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Test 2: Gateway Health Check
echo -e "${YELLOW}Test 2: Gateway Health Check (Circuit Breaker + Timeout)${NC}"
echo -e "${CYAN}Endpoint: GET /health/gateway${NC}"
echo ""

start_time=$(date +%s.%N)
response=$(curl -s -X GET "${BASE_URL}/health/gateway")
end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)

echo -e "${GREEN}Response:${NC} $response"
echo -e "${GREEN}Duration:${NC} ${duration}s"
echo ""

if echo "$response" | grep -q "healthy"; then
    echo -e "${GREEN}✓ SUCCESS: Gateway is healthy${NC}"
elif echo "$response" | grep -q "degraded"; then
    echo -e "${YELLOW}⚠ WARNING: Gateway health degraded (Circuit Breaker may be OPEN)${NC}"
else
    echo -e "${RED}✗ ERROR: Unexpected response${NC}"
fi

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Test 3: Async Payment Notification
echo -e "${YELLOW}Test 3: Payment Notification (Async + Bulkhead + Timeout + Fallback)${NC}"
echo -e "${CYAN}Endpoint: POST /notify/PAY-12345${NC}"
echo ""

start_time=$(date +%s.%N)
response=$(curl -s -X POST "${BASE_URL}/notify/PAY-12345")
end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)

echo -e "${GREEN}Response:${NC} $response"
echo -e "${GREEN}Duration:${NC} ${duration}s"
echo ""

if echo "$response" | grep -q "queued\|sent"; then
    echo -e "${GREEN}✓ SUCCESS: Notification queued/sent${NC}"
elif echo "$response" | grep -q "fallback"; then
    echo -e "${YELLOW}⚠ FALLBACK: Used fallback notification${NC}"
else
    echo -e "${RED}✗ ERROR: Unexpected response${NC}"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Basic tests complete!${NC}"
echo ""
echo -e "${CYAN}Next steps:${NC}"
echo -e "  • Run ${YELLOW}test-payment-retry.sh${NC} to test retry behavior"
echo -e "  • Run ${YELLOW}test-payment-circuit-breaker.sh${NC} to test circuit breaker states"
echo -e "  • Run ${YELLOW}test-payment-bulkhead.sh${NC} to test bulkhead limits"
echo -e "  • Check metrics at ${BLUE}http://localhost:9080/metrics?scope=base${NC}"
echo ""
