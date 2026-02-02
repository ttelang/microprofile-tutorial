#!/bin/bash

# Test script for MicroProfile E-Commerce Store OpenAPI v3.1 features
# Demonstrates JSON Schema 2020-12 validation capabilities

BASE_URL="http://localhost:5050/mp-ecomm-store/api"
OPENAPI_URL="http://localhost:5050/mp-ecomm-store/openapi"

echo "=========================================="
echo "E-Commerce Store - OpenAPI v3.1 Feature Tests"
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

echo "=========================================="
echo "OpenAPI v3.1 Feature Verification Complete"
echo "=========================================="
echo ""
echo "To view interactive documentation, open:"
echo "  http://localhost:5050/mp-ecomm-store/openapi/ui"
echo ""
echo "To view full OpenAPI spec:"
echo "  curl $OPENAPI_URL | jq '.'"
echo ""
echo "Key JSON Schema 2020-12 features demonstrated:"
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
