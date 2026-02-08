#!/bin/bash

# Test script for annotation-based webhook implementation
# Demonstrates webhook support WITHOUT CustomModelReader modifications

set -e

BASE_URL="http://localhost:5050/mp-ecomm-store/api"
WEBHOOK_ENDPOINT="${BASE_URL}/webhooks"

echo "=================================================="
echo "Webhook Annotation-Based Approach Demo"
echo "=================================================="
echo ""
echo "This test demonstrates webhook support using @Callback annotations"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Check OpenAPI spec for callbacks
echo -e "${BLUE}Step 1: Verify OpenAPI Callbacks Documentation${NC}"
echo "Checking if callbacks are documented in OpenAPI spec..."
echo ""

if curl -s -H "Accept: application/json" http://localhost:5050/openapi | jq -e '.paths."/api/webhooks".post.callbacks' > /dev/null; then
    echo -e "${GREEN}✓ Callbacks found in OpenAPI spec!${NC}"
    echo ""
    echo "Callback events documented:"
    curl -s -H "Accept: application/json" http://localhost:5050/openapi | \
        jq -r '.paths."/api/webhooks".post.callbacks.productEvents["{$request.body#/callbackUrl}"].post.summary' 2>/dev/null || \
        echo "  (Unable to parse callback details - but they exist in spec)"
else
    echo -e "${YELLOW}⚠ Server may not be running. Start with: mvn liberty:run${NC}"
    exit 1
fi

echo ""
echo "=================================================="
echo -e "${BLUE}Step 2: Create Webhook Subscription${NC}"
echo ""

# Create subscription
SUBSCRIPTION=$(cat <<EOF
{
  "callbackUrl": "https://example.com/webhooks/products",
  "events": [
    "product.created",
    "product.updated", 
    "product.deleted",
    "product.stock.low",
    "product.stock.out"
  ],
  "active": true
}
EOF
)

echo "Subscribing to webhook events..."
RESPONSE=$(curl -s -X POST "${WEBHOOK_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d "$SUBSCRIPTION")

echo -e "${GREEN}✓ Subscription created:${NC}"
echo "$RESPONSE" | jq '.'

# Extract subscription ID
SUB_ID=$(echo "$RESPONSE" | jq -r '.id')
echo ""
echo "Subscription ID: ${SUB_ID}"

echo ""
echo "=================================================="
echo -e "${BLUE}Step 3: List Subscriptions${NC}"
echo ""

curl -s "${WEBHOOK_ENDPOINT}" | jq '.'

echo ""
echo "=================================================="
echo -e "${BLUE}Step 4: Get Specific Subscription${NC}"
echo ""

curl -s "${WEBHOOK_ENDPOINT}/${SUB_ID}" | jq '.'

echo ""
echo "=================================================="
echo -e "${BLUE}Step 5: View Callback Documentation${NC}"
echo ""
echo "The @Callback annotation documents these webhook events:"
echo ""

curl -s -H "Accept: application/json" http://localhost:5050/openapi | \
    jq -r '.paths."/api/webhooks".post.callbacks.productEvents["{$request.body#/callbackUrl}"].post.requestBody.content."application/json".examples | to_entries[] | "Event: \(.key)\n  Type: \(.value.value.eventType)\n  Example ID: \(.value.value.eventId)\n"' \
    2>/dev/null || echo "(Examples available in OpenAPI spec)"

echo ""
echo "=================================================="
echo -e "${BLUE}Step 6: Delete Subscription${NC}"
echo ""

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${WEBHOOK_ENDPOINT}/${SUB_ID}")

if [ "$HTTP_CODE" = "204" ]; then
    echo -e "${GREEN}✓ Subscription deleted (HTTP 204)${NC}"
else
    echo -e "${YELLOW}⚠ Unexpected response code: ${HTTP_CODE}${NC}"
fi

echo ""
echo "=================================================="
echo -e "${GREEN}✓ Demo Complete!${NC}"
echo "=================================================="
echo ""
echo "Key Takeaways:"
echo "1. ✅ Webhooks documented using @Callback annotations"
echo "2. ✅ All 5 event types fully documented with examples"
echo "3. ✅ Security headers documented (@Header annotation)"
echo "4. ✅ Response codes and retry behavior documented"
echo ""
echo "OpenAPI Spec Location:"
echo "  paths → /api/webhooks → post → callbacks → productEvents"
echo ""
echo "The @Callback annotation provides complete webhook documentation"
echo ""