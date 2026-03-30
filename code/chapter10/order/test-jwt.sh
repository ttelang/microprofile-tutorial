#!/bin/bash
# Basic JWT smoke test for chapter10 order and user services.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TOKEN_FILE="$SCRIPT_DIR/../tools/token.jwt"
JWTENIZR_JAR="$SCRIPT_DIR/../tools/jwtenizr.jar"
TOKEN="${JWT_TOKEN:-}"

if [[ -z "$TOKEN" ]]; then
  if [[ ! -f "$JWTENIZR_JAR" ]]; then
    echo "jwtenizr not found. Expected: $JWTENIZR_JAR"
    echo "See code/chapter10/order/README.adoc -> Get jwtenizr In The tools Folder"
    exit 1
  fi

  echo "Regenerating JWT token using jwtenizr..."
  (
    cd "$SCRIPT_DIR/../tools"
    java -jar jwtenizr.jar >/dev/null
  )

  if [[ ! -f "$TOKEN_FILE" ]]; then
    echo "JWT token not found. Expected: $TOKEN_FILE"
    echo "Set JWT_TOKEN env var or generate code/chapter10/tools/token.jwt first."
    exit 1
  fi
  TOKEN="$(tr -d '\n' < "$TOKEN_FILE")"
fi

ORDER_BASE_URL="${ORDER_BASE_URL:-http://localhost:8050/order/api}"
USER_BASE_URL="${USER_BASE_URL:-http://localhost:6050/user/api}"

echo "Using ORDER_BASE_URL=$ORDER_BASE_URL"
echo "Using USER_BASE_URL=$USER_BASE_URL"

echo "[1/3] Creating order as user role..."
CREATE_RESPONSE=$(curl -sS -w "\n%{http_code}" -X POST "$ORDER_BASE_URL/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "user1",
    "customerEmail": "user1@example.com",
    "currency": "USD",
    "items": [
      {
        "productId": "p-100",
        "productName": "Keyboard",
        "quantity": 1,
        "unitPrice": 49.99
      }
    ],
    "shippingAddress": {
      "street": "1 Main St",
      "city": "Austin",
      "state": "TX",
      "zipCode": "78701",
      "country": "US"
    },
    "billingAddress": {
      "street": "1 Main St",
      "city": "Austin",
      "state": "TX",
      "zipCode": "78701",
      "country": "US"
    }
  }')
CREATE_BODY=$(echo "$CREATE_RESPONSE" | head -n -1)
CREATE_STATUS=$(echo "$CREATE_RESPONSE" | tail -n 1)

if [[ "$CREATE_STATUS" != "201" ]]; then
  echo "Order create failed: HTTP $CREATE_STATUS"
  echo "$CREATE_BODY"
  exit 1
fi

ORDER_ID=$(echo "$CREATE_BODY" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p' | head -n 1)
if [[ -z "$ORDER_ID" ]]; then
  echo "Could not parse order id from create response"
  echo "$CREATE_BODY"
  exit 1
fi

echo "Created order id=$ORDER_ID"

echo "[2/3] Fetching order as user role..."
GET_STATUS=$(curl -sS -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  "$ORDER_BASE_URL/orders/$ORDER_ID")

if [[ "$GET_STATUS" != "200" ]]; then
  echo "Order fetch failed: HTTP $GET_STATUS"
  exit 1
fi

echo "[3/3] Fetching user profile..."
PROFILE_STATUS=$(curl -sS -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  "$USER_BASE_URL/users/user-profile")

if [[ "$PROFILE_STATUS" != "200" ]]; then
  echo "User profile fetch failed: HTTP $PROFILE_STATUS"
  exit 1
fi

echo "JWT smoke test passed."