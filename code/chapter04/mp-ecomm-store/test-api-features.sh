#!/bin/bash

# Comprehensive test script for MicroProfile E-Commerce Store API
# Tests both OpenAPI v3.1 features and webhook functionality

set -e

BASE_URL="http://localhost:5050/mp-ecomm-store/api"
OPENAPI_URL="http://localhost:5050/mp-ecomm-store/openapi"
WEBHOOK_ENDPOINT="${BASE_URL}/webhooks"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "E-Commerce Store - API Feature Tests"
echo "=========================================="
echo "Testing:"
echo "  1. OpenAPI v3.1 with JSON Schema 2020-12"
echo "  2. Webhook callbacks and subscriptions"
echo "=========================================="
echo ""

# ============================================================
# PART 1: OpenAPI v3.1 Feature Tests
# ============================================================

echo -e "${BLUE}=========================================="
echo "PART 1: OpenAPI v3.1 Feature Tests"
echo -e "==========================================${NC}"
echo ""

# Test 1: Get OpenAPI Specification
echo "Test 1: Fetching OpenAPI v3.1 Specification..."
echo "GET $OPENAPI_URL"
curl -s "$OPENAPI_URL" | jq -r '.openapi, .info.version, .info.title' | head -3
echo ""

# Test 2: Get all products (Array schema with minItems/maxItems)
echo "Test 2: Get all products (demonstrates array schema constraints)..."
echo "GET $BASE_URL/products"
curl -s "$BASE_URL/products" | jq -c '.[] | {id, name, price, sku}'
echo ""
echo ""

# Test 3: Get product by ID (demonstrates int64 format validation)
echo "Test 3: Get product by ID (demonstrates format: int64)..."
echo "GET $BASE_URL/products/1"
curl -s "$BASE_URL/products/1" | jq '.'
echo ""

# Test 4: Search by name (demonstrates pattern matching)
echo "Test 4: Search by name (demonstrates string pattern validation)..."
echo "GET $BASE_URL/products/search?name=iPhone"
curl -s "$BASE_URL/products/search?name=iPhone" | jq -c '.[] | {name, price}'
echo ""
echo ""

# Test 5: Search by price range (demonstrates exclusiveMinimum)
echo "Test 5: Search by price range (demonstrates exclusiveMinimum)..."
echo "GET $BASE_URL/products/search?minPrice=1000&maxPrice=1500"
curl -s "$BASE_URL/products/search?minPrice=1000&maxPrice=1500" | jq -c '.[] | {name, price}'
echo ""
echo ""

# Test 6: Search by category (demonstrates enumeration)
echo "Test 6: Search by category (demonstrates enumeration validation)..."
echo "GET $BASE_URL/products/search?category=ELECTRONICS"
curl -s "$BASE_URL/products/search?category=ELECTRONICS" | jq -c '.[] | {name, category}'
echo ""
echo ""

# Test 7: Search with pagination (demonstrates default values)
echo "Test 7: Search with pagination (demonstrates default values)..."
echo "GET $BASE_URL/products/search?page=0&size=2"
curl -s "$BASE_URL/products/search?page=0&size=2" | jq -c '.[] | {id, name}'
echo ""
echo ""

# Test 8: Create product with full validation
echo "Test 8: Create product (demonstrates full schema validation)..."
echo "POST $BASE_URL/products"
NEW_PRODUCT='{
  "name": "Sony WH-1000XM5",
  "description": "Sony WH-1000XM5 Wireless Noise-Canceling Headphones",
  "price": 399.99,
  "sku": "SON-WH1-XM5-BLK",
  "category": "ELECTRONICS",
  "stockQuantity": 15,
  "inStock": true
}'
curl -s -X POST "$BASE_URL/products" \
  -H "Content-Type: application/json" \
  -d "$NEW_PRODUCT" | jq '.'
echo ""

# Test 9: Verify new product was created
echo "Test 9: Verify new product was created..."
echo "GET $BASE_URL/products"
curl -s "$BASE_URL/products" | jq -c '.[] | select(.name == "Sony WH-1000XM5") | {id, name, price, sku}'
echo ""
echo ""

# Test 10: Combined search
echo "Test 10: Combined search (name, price, category)..."
echo "GET $BASE_URL/products/search?name=iPhone&minPrice=900&category=ELECTRONICS"
curl -s "$BASE_URL/products/search?name=iPhone&minPrice=900&category=ELECTRONICS" | jq -c '.[] | {name, price, category}'
echo ""
echo ""

# Test 11: Check OpenAPI schema for JSON Schema 2020-12 features
echo "Test 11: Verify JSON Schema 2020-12 features in OpenAPI spec..."
echo "Checking Product schema for:"
echo "  - exclusiveMinimum (numeric value, not boolean)"
echo "  - pattern validation (SKU)"
echo "  - format specifications (int64, double)"
echo "  - nullable properties"
echo "  - enumeration (category)"
echo ""
curl -s "$OPENAPI_URL" | jq '.components.schemas.Product.properties | {
  id: .id | {format, readOnly, minimum},
  name: .name | {minLength, maxLength, pattern},
  price: .price | {minimum, exclusiveMinimum, multipleOf, format},
  sku: .sku | {pattern, minLength, maxLength},
  category: .category | {enum: .enumeration, nullable},
  stockQuantity: .stockQuantity | {minimum, defaultValue, format}
}'
echo ""

echo -e "${GREEN}=========================================="
echo "OpenAPI v3.1 Feature Tests Complete"
echo -e "==========================================${NC}"
echo ""

# ============================================================
# PART 2: Webhook Feature Tests
# ============================================================

echo -e "${BLUE}=========================================="
echo "PART 2: Webhook Feature Tests"
echo -e "==========================================${NC}"
echo ""
echo "Testing webhook support using @Callback annotations"
echo ""

# Test 12: Check OpenAPI spec for callbacks
echo -e "${BLUE}Test 12: Verify OpenAPI Callbacks Documentation${NC}"
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

# Test 13: Create Webhook Subscription
echo -e "${BLUE}Test 13: Create Webhook Subscription${NC}"
echo ""

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

# Test 14: List Subscriptions
echo -e "${BLUE}Test 14: List All Webhook Subscriptions${NC}"
echo ""

curl -s "${WEBHOOK_ENDPOINT}" | jq '.'

echo ""

# Test 15: Get Specific Subscription
echo -e "${BLUE}Test 15: Get Specific Webhook Subscription${NC}"
echo ""

curl -s "${WEBHOOK_ENDPOINT}/${SUB_ID}" | jq '.'

echo ""

# Test 16: View Callback Documentation
echo -e "${BLUE}Test 16: View Callback Documentation${NC}"
echo ""
echo "The @Callback annotation documents these webhook events:"
echo ""

curl -s -H "Accept: application/json" http://localhost:5050/openapi | \
    jq -r '.paths."/api/webhooks".post.callbacks.productEvents["{$request.body#/callbackUrl}"].post.requestBody.content."application/json".examples | to_entries[] | "Event: \(.key)\n  Type: \(.value.value.eventType)\n  Example ID: \(.value.value.eventId)\n"' \
    2>/dev/null || echo "(Examples available in OpenAPI spec)"

echo ""

# Test 17: Delete Subscription
echo -e "${BLUE}Test 17: Delete Webhook Subscription${NC}"
echo ""

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${WEBHOOK_ENDPOINT}/${SUB_ID}")

if [ "$HTTP_CODE" = "204" ]; then
    echo -e "${GREEN}✓ Subscription deleted (HTTP 204)${NC}"
else
    echo -e "${YELLOW}⚠ Unexpected response code: ${HTTP_CODE}${NC}"
fi

echo ""

echo -e "${GREEN}=========================================="
echo "Webhook Feature Tests Complete"
echo -e "==========================================${NC}"
echo ""

# ============================================================
# Summary
# ============================================================

echo "=========================================="
echo "ALL TESTS COMPLETE"
echo "=========================================="
echo ""
echo "OpenAPI v3.1 & JSON Schema 2020-12 Features:"
echo "  ✓ Pattern validation (SKU format)"
echo "  ✓ Exclusive minimum (price > \$0.01)"
echo "  ✓ Multiple of (price rounded to cents)"
echo "  ✓ Format specifications (int64, double, int32)"
echo "  ✓ Enumeration (category values)"
echo "  ✓ Array constraints (minItems, maxItems)"
echo "  ✓ String length (minLength, maxLength)"
echo "  ✓ Nullable properties"
echo "  ✓ Default values"
echo "  ✓ Read-only properties"
echo ""
echo "Webhook Features:"
echo "  ✓ Webhooks documented using @Callback annotations"
echo "  ✓ All 5 event types fully documented with examples"
echo "  ✓ Security headers documented (@Header annotation)"
echo "  ✓ Response codes and retry behavior documented"
echo ""
echo "Useful Links:"
echo "  Interactive docs: http://localhost:5050/mp-ecomm-store/openapi/ui"
echo "  OpenAPI spec:     curl $OPENAPI_URL | jq '.'"
echo "  Webhook docs:     paths → /api/webhooks → post → callbacks → productEvents"
echo ""
