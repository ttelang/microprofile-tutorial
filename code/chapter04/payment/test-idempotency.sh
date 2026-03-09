#!/bin/bash

# Test script for payment idempotency
# This script demonstrates how the PUT endpoint handles idempotent payment requests

BASE_URL="http://localhost:9080/payment/api/payments"

# Generate a unique payment ID (you can also use uuidgen if available)
PAYMENT_ID="550e8400-e29b-41d4-a716-446655440000"

echo "=========================================="
echo "Testing Payment Idempotency with PUT"
echo "=========================================="
echo ""

# Test 1: First payment request
echo "Test 1: First payment request"
echo "PUT ${BASE_URL}/${PAYMENT_ID}"
echo ""

RESPONSE=$(curl -X PUT "${BASE_URL}/${PAYMENT_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111",
    "cardHolderName": "Jane Smith",
    "expiryDate": "12/25",
    "securityCode": "456",
    "amount": 150.00
  }' \
  -w "\n%{http_code}" \
  -s)

HTTP_STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "$BODY" | jq .
echo "HTTP Status: $HTTP_STATUS"
echo ""
echo "Expected: cached=false (payment processed for the first time)"
echo ""
read -p "Press Enter to continue to Test 2..."
echo ""

# Test 2: Retry with same payment ID and details
echo "Test 2: Retry with same payment ID and details (idempotent)"
echo "PUT ${BASE_URL}/${PAYMENT_ID}"
echo ""

RESPONSE=$(curl -X PUT "${BASE_URL}/${PAYMENT_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111",
    "cardHolderName": "Jane Smith",
    "expiryDate": "12/25",
    "securityCode": "456",
    "amount": 150.00
  }' \
  -w "\n%{http_code}" \
  -s)

HTTP_STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "$BODY" | jq .
echo "HTTP Status: $HTTP_STATUS"
echo ""
echo "Expected: cached=true (cached result returned, no reprocessing)"
echo ""
read -p "Press Enter to continue to Test 3..."
echo ""

# Test 3: Same payment ID with different details
echo "Test 3: Same payment ID with different payment amount (should fail)"
echo "PUT ${BASE_URL}/${PAYMENT_ID}"
echo ""

RESPONSE=$(curl -X PUT "${BASE_URL}/${PAYMENT_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111",
    "cardHolderName": "Jane Smith",
    "expiryDate": "12/25",
    "securityCode": "456",
    "amount": 200.00
  }' \
  -w "\n%{http_code}" \
  -s)

HTTP_STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "$BODY" | jq .
echo "HTTP Status: $HTTP_STATUS"
echo ""
echo "Expected: HTTP 409 Conflict (same payment ID with different details)"
echo ""
read -p "Press Enter to continue to Test 4..."
echo ""

# Test 4: New payment with different ID
NEW_PAYMENT_ID="650e8400-e29b-41d4-a716-446655440001"
echo "Test 4: New payment with different payment ID"
echo "PUT ${BASE_URL}/${NEW_PAYMENT_ID}"
echo ""

RESPONSE=$(curl -X PUT "${BASE_URL}/${NEW_PAYMENT_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "5500000000000004",
    "cardHolderName": "John Doe",
    "expiryDate": "06/26",
    "securityCode": "789",
    "amount": 99.99
  }' \
  -w "\n%{http_code}" \
  -s)

HTTP_STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "$BODY" | jq .
echo "HTTP Status: $HTTP_STATUS"
echo ""
echo "Expected: cached=false (new payment processed)"
echo ""

echo "=========================================="
echo "Idempotency Tests Complete!"
echo "=========================================="
