#!/bin/bash

# Test script for OpenAPI v3.1 features demonstration
# This script tests various endpoints showcasing JSON Schema 2020-12 features

BASE_URL="http://localhost:5050/catalog/api"
OPENAPI_URL="http://localhost:5050/catalog/openapi"

echo "=========================================="
echo "MicroProfile OpenAPI 4.1 - OpenAPI v3.1 Feature Tests"
echo "=========================================="
echo ""

# Test 1: Get OpenAPI Specification
echo "Test 1: Fetching OpenAPI v3.1 Specification..."
echo "GET $OPENAPI_URL"
curl -s "$OPENAPI_URL" | jq -r '.openapi, .info.version, .info.title' | head -3
echo ""

# Test 2: Get all products (Array schema with minItems/maxItems)
echo "Test 2: Get all products (demonstrates array schema constraints)..."
echo "GET $BASE_URL/products"
curl -s "$BASE_URL/products" | jq -c '.[0:2]'
echo ""
echo ""

# Test 3: Get product by ID (demonstrates int64 format validation)
echo "Test 3: Get product by ID (demonstrates format: int64)..."
echo "GET $BASE_URL/products/1"
curl -s "$BASE_URL/products/1" | jq '.'
echo ""

# Test 4: Search with price range (demonstrates exclusiveMinimum)
echo "Test 4: Search by price range (demonstrates exclusiveMinimum)..."
echo "GET $BASE_URL/products/search?minPrice=500&maxPrice=2000"
curl -s "$BASE_URL/products/search?minPrice=500&maxPrice=2000" | jq -c '.[] | {name, price}'
echo ""
echo ""

# Test 5: Search by category (demonstrates enumeration)
echo "Test 5: Search by category (demonstrates enumeration validation)..."
echo "GET $BASE_URL/products/search?category=ELECTRONICS"
curl -s "$BASE_URL/products/search?category=ELECTRONICS" | jq -c '.[] | {name, category}'
echo ""
echo ""

# Test 6: Search with pagination (demonstrates default values)
echo "Test 6: Search with pagination (demonstrates default values)..."
echo "GET $BASE_URL/products/search?page=0&size=2"
curl -s "$BASE_URL/products/search?page=0&size=2" | jq -c '.[] | {id, name}'
echo ""
echo ""

# Test 7: Create product with full validation
echo "Test 7: Create product (demonstrates full schema validation)..."
echo "POST $BASE_URL/products"
NEW_PRODUCT='{
  "name": "Samsung Galaxy S24",
  "description": "Samsung Galaxy S24 Ultra with 512GB storage",
  "price": 1299.99,
  "sku": "SAM-GAL-S24-512",
  "category": "ELECTRONICS",
  "stockQuantity": 30,
  "rating": 4.7,
  "weight": 0.234,
  "manufacturer": "Samsung Electronics",
  "warrantyMonths": 24
}'
curl -s -X POST "$BASE_URL/products" \
  -H "Content-Type: application/json" \
  -d "$NEW_PRODUCT" | jq '.'
echo ""

# Test 8: Try invalid SKU pattern (should fail validation)
echo "Test 8: Test SKU pattern validation (should fail with invalid format)..."
echo "POST $BASE_URL/products (with invalid SKU)"
INVALID_PRODUCT='{
  "name": "Test Product",
  "description": "Test description",
  "price": 99.99,
  "sku": "invalid-sku-format"
}'
curl -s -X POST "$BASE_URL/products" \
  -H "Content-Type: application/json" \
  -d "$INVALID_PRODUCT" | jq '.' || echo "Expected validation error"
echo ""
echo ""

# Test 9: Check OpenAPI schema for JSON Schema 2020-12 features
echo "Test 9: Verify JSON Schema 2020-12 features in OpenAPI spec..."
echo "Checking for:"
echo "  - exclusiveMinimum (numeric value, not boolean)"
echo "  - pattern validation"
echo "  - format specifications"
echo "  - nullable properties"
echo ""
curl -s "$OPENAPI_URL" | jq '.components.schemas.Product.properties | {
  price: .price | {minimum, exclusiveMinimum, multipleOf, format},
  sku: .sku | {pattern, minLength, maxLength},
  category: .category | {enum: .enumeration, nullable},
  id: .id | {format, readOnly, minimum}
}'
echo ""

echo "=========================================="
echo "OpenAPI v3.1 Feature Verification Complete"
echo "=========================================="
echo ""
echo "To view interactive documentation, open:"
echo "  http://localhost:5050/catalog/openapi/ui"
echo ""
echo "To view full OpenAPI spec:"
echo "  curl $OPENAPI_URL | jq '.'"
echo ""
