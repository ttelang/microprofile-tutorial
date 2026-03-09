#!/bin/bash

# Test script for Payment Audit Trail functionality
# This script demonstrates how audit records are created and can be queried

BASE_URL="http://localhost:9080/payment/api"

echo "=========================================="
echo "Testing Payment Audit Trail System"
echo "=========================================="
echo ""

# Test 1: Process a payment and generate audit record
echo "Test 1: Process a valid payment (creates audit record)"
echo "POST ${BASE_URL}/payments/process"
echo ""

curl -X POST "${BASE_URL}/payments/process" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111",
    "cardHolderName": "Alice Johnson",
    "expiryDate": "12/25",
    "securityCode": "456",
    "amount": 250.00
  }' \
  -s | jq .

echo ""
read -p "Press Enter to continue to Test 2..."
echo ""

# Test 2: Validate payment details (creates audit record)
echo "Test 2: Validate payment details (creates audit record)"
echo "POST ${BASE_URL}/payments/validate"
echo ""

curl -X POST "${BASE_URL}/payments/validate" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "5555555555554444",
    "cardHolderName": "Bob Smith",
    "expiryDate": "06/26",
    "securityCode": "789",
    "amount": 150.00
  }' \
  -s | jq .

echo ""
read -p "Press Enter to continue to Test 3..."
echo ""

# Test 3: Process refund (creates audit record)
echo "Test 3: Process a refund (creates audit record)"
echo "POST ${BASE_URL}/payments/refund?amount=75.00"
echo ""

curl -X POST "${BASE_URL}/payments/refund?amount=75.00" \
  -H "Content-Type: application/json" \
  -s | jq .

echo ""
read -p "Press Enter to continue to Test 4..."
echo ""

# Test 4: Invalid payment validation (creates audit record)
echo "Test 4: Validate invalid payment - empty name (creates audit record)"
echo "POST ${BASE_URL}/payments/validate"
echo ""

curl -X POST "${BASE_URL}/payments/validate" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111",
    "cardHolderName": "",
    "expiryDate": "12/25",
    "securityCode": "456",
    "amount": 100.00
  }' \
  -s | jq .

echo ""
read -p "Press Enter to continue to Test 5..."
echo ""

# Test 5: Idempotent payment (creates audit record)
PAYMENT_ID="audit-test-550e8400-e29b-41d4-a716-446655440000"
echo "Test 5: Idempotent payment processing (creates audit record)"
echo "PUT ${BASE_URL}/payments/${PAYMENT_ID}"
echo ""

curl -X PUT "${BASE_URL}/payments/${PAYMENT_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "378282246310005",
    "cardHolderName": "Carol Davis",
    "expiryDate": "03/27",
    "securityCode": "1234",
    "amount": 500.00
  }' \
  -s | jq .

echo ""
read -p "Press Enter to continue to Test 6..."
echo ""

# Test 6: Retry idempotent payment (creates cache hit audit record)
echo "Test 6: Retry same idempotent payment (creates cache hit audit record)"
echo "PUT ${BASE_URL}/payments/${PAYMENT_ID}"
echo ""

curl -X PUT "${BASE_URL}/payments/${PAYMENT_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "378282246310005",
    "cardHolderName": "Carol Davis",
    "expiryDate": "03/27",
    "securityCode": "1234",
    "amount": 500.00
  }' \
  -s | jq .

echo ""
echo "=========================================="
echo "Now querying audit records..."
echo "=========================================="
echo ""
read -p "Press Enter to view audit records..."
echo ""

# Query 1: Get all audit records
echo "Query 1: Get all audit records"
echo "GET ${BASE_URL}/audit/records"
echo ""

curl -X GET "${BASE_URL}/audit/records" \
  -H "Content-Type: application/json" \
  -s | jq .

echo ""
read -p "Press Enter to continue to Query 2..."
echo ""

# Query 2: Get audit records for specific payment ID
echo "Query 2: Get audit records for payment ID: ${PAYMENT_ID}"
echo "GET ${BASE_URL}/audit/payment/${PAYMENT_ID}"
echo ""

curl -X GET "${BASE_URL}/audit/payment/${PAYMENT_ID}" \
  -H "Content-Type: application/json" \
  -s | jq .

echo ""
read -p "Press Enter to continue to Query 3..."
echo ""

# Query 3: Get audit records by operation type
echo "Query 3: Get audit records by operation type (PAYMENT_VALIDATE)"
echo "GET ${BASE_URL}/audit/operation/PAYMENT_VALIDATE"
echo ""

curl -X GET "${BASE_URL}/audit/operation/PAYMENT_VALIDATE" \
  -H "Content-Type: application/json" \
  -s | jq .

echo ""
read -p "Press Enter to continue to Query 4..."
echo ""

# Query 4: Get audit records by status
echo "Query 4: Get audit records by status (VALIDATION_ERROR)"
echo "GET ${BASE_URL}/audit/status/VALIDATION_ERROR"
echo ""

curl -X GET "${BASE_URL}/audit/status/VALIDATION_ERROR" \
  -H "Content-Type: application/json" \
  -s | jq .

echo ""
read -p "Press Enter to continue to Query 5..."
echo ""

# Query 5: Get audit statistics
echo "Query 5: Get audit statistics summary"
echo "GET ${BASE_URL}/audit/stats"
echo ""

curl -X GET "${BASE_URL}/audit/stats" \
  -H "Content-Type: application/json" \
  -s | jq .

echo ""
echo "=========================================="
echo "Audit Trail Tests Complete!"
echo "=========================================="
echo ""
echo "Summary:"
echo "- All payment operations create audit records"
echo "- Audit records capture: operation type, status, masked card info, amount, duration"
echo "- Audit trails can be queried by payment ID, operation type, status"
echo "- Audit statistics provide overview of all operations"
echo "- Check server logs for detailed AUDIT log entries"
echo ""
