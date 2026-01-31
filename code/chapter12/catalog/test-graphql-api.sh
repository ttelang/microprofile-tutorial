#!/bin/bash

# GraphQL API Test Script
# This script demonstrates various GraphQL queries and mutations

BASE_URL="http://localhost:5060/graphql-catalog/graphql"

echo "========================================="
echo "MicroProfile GraphQL API Test Script"
echo "========================================="
echo ""

# Function to execute GraphQL query
graphql_query() {
    local query=$1
    local description=$2
    
    echo "-------------------------------------------"
    echo "Test: $description"
    echo "-------------------------------------------"
    echo "Query:"
    echo "$query"
    echo ""
    echo "Response:"
    
    curl -s -X POST "$BASE_URL" \
        -H "Content-Type: application/json" \
        -d "{\"query\": \"$query\"}" | jq .
    
    echo ""
    echo ""
}

# Test 1: Query all products
graphql_query "{ products { id name price category } }" \
    "Get all products"

# Test 2: Query single product with reviews
graphql_query "{ product(id: 1) { id name price priceWithTax reviews { reviewerName rating comment } } }" \
    "Get product with reviews"

# Test 3: Search products
graphql_query "{ searchProducts(searchTerm: \"laptop\") { id name price } }" \
    "Search for laptops"

# Test 4: Get statistics
graphql_query "{ productCount averagePrice categories }" \
    "Get catalog statistics"

# Test 5: Query with computed fields
graphql_query "{ products { id name price priceWithTax priceCategory availabilityStatus } }" \
    "Products with computed fields"

# Test 6: Top reviews
graphql_query "{ product(id: 1) { name topReviews(limit: 2) { reviewerName rating } } }" \
    "Get top 2 reviews for product"

# Test 7: Create product (mutation)
MUTATION='mutation { createProduct(input: { name: \"Test Product\" description: \"A test product\" price: 99.99 category: \"Test\" stockQuantity: 10 }) { id name price } }'
graphql_query "$MUTATION" \
    "Create new product"

# Test 8: Error handling - non-existent product
graphql_query "{ product(id: 9999) { id name } }" \
    "Query non-existent product (error handling)"

echo "========================================="
echo "Tests completed!"
echo "========================================="
