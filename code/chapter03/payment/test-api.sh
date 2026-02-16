#!/bin/bash
# filepath: /workspaces/microprofile-tutorial/code/chapter03/payment/test-api.sh

# MicroProfile Payment Service API Test Script
# This script tests all payment operations including process, validate, and refund

set -e  # Exit on any error

# Configuration
BASE_URL="http://localhost:9080/payment/api/payments"
CONTENT_TYPE="Content-Type: application/json"

echo "=================================="
echo "MicroProfile Payment Service API Test"
echo "=================================="
echo "Base URL: $BASE_URL"
echo "=================================="

# Function to print section headers
print_section() {
    echo ""
    echo "--- $1 ---"
}

# Function to pause between operations
pause() {
    echo "Press Enter to continue..."
    read -r
}

# Valid payment details JSON
VALID_PAYMENT='{
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "securityCode": "123",
  "amount": 99.99
}'

# Invalid payment details JSON (short card number)
INVALID_PAYMENT_SHORT_CARD='{
  "cardNumber": "4111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "securityCode": "123",
  "amount": 99.99
}'

# Invalid payment details JSON (invalid expiry date format)
INVALID_PAYMENT_EXPIRY='{
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "13-2025",
  "securityCode": "123",
  "amount": 99.99
}'

# Invalid payment details JSON (invalid security code)
INVALID_PAYMENT_CVV='{
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "securityCode": "12",
  "amount": 99.99
}'

# Invalid payment details JSON (zero amount)
INVALID_PAYMENT_AMOUNT='{
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "securityCode": "123",
  "amount": 0
}'

# Invalid payment details JSON (empty cardholder name)
INVALID_PAYMENT_NAME='{
  "cardNumber": "4111111111111111",
  "cardHolderName": "",
  "expiryDate": "12/25",
  "securityCode": "123",
  "amount": 99.99
}'

print_section "1. VALIDATE VALID PAYMENT DETAILS"
echo "Command: curl -i -X POST $BASE_URL/validate -H \"$CONTENT_TYPE\" -d '$VALID_PAYMENT'"
curl -i -X POST "$BASE_URL/validate" \
  -H "$CONTENT_TYPE" \
  -d "$VALID_PAYMENT"
echo ""
pause

print_section "2. VALIDATE INVALID PAYMENT (Short Card Number)"
echo "Command: curl -i -X POST $BASE_URL/validate -H \"$CONTENT_TYPE\" -d '$INVALID_PAYMENT_SHORT_CARD'"
curl -i -X POST "$BASE_URL/validate" \
  -H "$CONTENT_TYPE" \
  -d "$INVALID_PAYMENT_SHORT_CARD"
echo ""
pause

print_section "3. VALIDATE INVALID PAYMENT (Invalid Expiry Date Format)"
echo "Command: curl -i -X POST $BASE_URL/validate -H \"$CONTENT_TYPE\" -d '$INVALID_PAYMENT_EXPIRY'"
curl -i -X POST "$BASE_URL/validate" \
  -H "$CONTENT_TYPE" \
  -d "$INVALID_PAYMENT_EXPIRY"
echo ""
pause

print_section "4. VALIDATE INVALID PAYMENT (Invalid Security Code)"
echo "Command: curl -i -X POST $BASE_URL/validate -H \"$CONTENT_TYPE\" -d '$INVALID_PAYMENT_CVV'"
curl -i -X POST "$BASE_URL/validate" \
  -H "$CONTENT_TYPE" \
  -d "$INVALID_PAYMENT_CVV"
echo ""
pause

print_section "5. VALIDATE INVALID PAYMENT (Zero Amount)"
echo "Command: curl -i -X POST $BASE_URL/validate -H \"$CONTENT_TYPE\" -d '$INVALID_PAYMENT_AMOUNT'"
curl -i -X POST "$BASE_URL/validate" \
  -H "$CONTENT_TYPE" \
  -d "$INVALID_PAYMENT_AMOUNT"
echo ""
pause

print_section "6. VALIDATE INVALID PAYMENT (Empty Cardholder Name)"
echo "Command: curl -i -X POST $BASE_URL/validate -H \"$CONTENT_TYPE\" -d '$INVALID_PAYMENT_NAME'"
curl -i -X POST "$BASE_URL/validate" \
  -H "$CONTENT_TYPE" \
  -d "$INVALID_PAYMENT_NAME"
echo ""
pause

print_section "7. PROCESS VALID PAYMENT"
echo "Command: curl -i -X POST $BASE_URL/process -H \"$CONTENT_TYPE\" -d '$VALID_PAYMENT'"
curl -i -X POST "$BASE_URL/process" \
  -H "$CONTENT_TYPE" \
  -d "$VALID_PAYMENT"
echo ""
pause

print_section "8. PROCESS INVALID PAYMENT (Should Fail Validation)"
echo "Command: curl -i -X POST $BASE_URL/process -H \"$CONTENT_TYPE\" -d '$INVALID_PAYMENT_SHORT_CARD'"
curl -i -X POST "$BASE_URL/process" \
  -H "$CONTENT_TYPE" \
  -d "$INVALID_PAYMENT_SHORT_CARD"
echo ""
pause

print_section "9. PROCESS REFUND (Valid Amount)"
echo "Command: curl -i -X POST '$BASE_URL/refund?amount=50.00' -H \"$CONTENT_TYPE\""
curl -i -X POST "$BASE_URL/refund?amount=50.00" \
  -H "$CONTENT_TYPE"
echo ""
pause

print_section "10. PROCESS REFUND (Valid Amount - Different)"
echo "Command: curl -i -X POST '$BASE_URL/refund?amount=25.50' -H \"$CONTENT_TYPE\""
curl -i -X POST "$BASE_URL/refund?amount=25.50" \
  -H "$CONTENT_TYPE"
echo ""
pause

print_section "11. PROCESS REFUND (Invalid - Zero Amount)"
echo "Command: curl -i -X POST '$BASE_URL/refund?amount=0' -H \"$CONTENT_TYPE\""
curl -i -X POST "$BASE_URL/refund?amount=0" \
  -H "$CONTENT_TYPE"
echo ""
pause

print_section "12. PROCESS REFUND (Invalid - Negative Amount)"
echo "Command: curl -i -X POST '$BASE_URL/refund?amount=-10.00' -H \"$CONTENT_TYPE\""
curl -i -X POST "$BASE_URL/refund?amount=-10.00" \
  -H "$CONTENT_TYPE"
echo ""
pause

print_section "13. PROCESS PAYMENT WITH DIFFERENT AMOUNT"
PAYMENT_LARGE_AMOUNT='{
  "cardNumber": "5555555555554444",
  "cardHolderName": "Jane Smith",
  "expiryDate": "06/27",
  "securityCode": "456",
  "amount": 1500.00
}'
echo "Command: curl -i -X POST $BASE_URL/process -H \"$CONTENT_TYPE\" -d '$PAYMENT_LARGE_AMOUNT'"
curl -i -X POST "$BASE_URL/process" \
  -H "$CONTENT_TYPE" \
  -d "$PAYMENT_LARGE_AMOUNT"
echo ""
pause

print_section "14. PROCESS PAYMENT WITH 4-DIGIT CVV"
PAYMENT_4_DIGIT_CVV='{
  "cardNumber": "378282246310005",
  "cardHolderName": "Bob Johnson",
  "expiryDate": "03/26",
  "securityCode": "1234",
  "amount": 299.99
}'
echo "Command: curl -i -X POST $BASE_URL/process -H \"$CONTENT_TYPE\" -d '$PAYMENT_4_DIGIT_CVV'"
curl -i -X POST "$BASE_URL/process" \
  -H "$CONTENT_TYPE" \
  -d "$PAYMENT_4_DIGIT_CVV"
echo ""

echo ""
echo "=================================="
echo "Payment Service API Tests Completed"
echo "=================================="
