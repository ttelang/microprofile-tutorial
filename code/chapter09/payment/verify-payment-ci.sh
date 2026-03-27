#!/usr/bin/env bash

set -u
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_CURL_TIMEOUT="${DEFAULT_CURL_TIMEOUT:-15}"
DEFAULT_CURL_CONNECT_TIMEOUT="${DEFAULT_CURL_CONNECT_TIMEOUT:-5}"
BASE_URL="${BASE_URL:-http://localhost:9080}"
API_BASE="${API_BASE:-${BASE_URL}/payment/api}"
HEALTH_URL="${HEALTH_URL:-${BASE_URL}/health}"
METRICS_URL="${METRICS_URL:-${BASE_URL}/metrics}"
OPENAPI_URL="${OPENAPI_URL:-${BASE_URL}/api/openapi}"
JAEGER_URL="${JAEGER_URL:-http://localhost:16686}"
JAEGER_OTLP_GRPC_PORT="${JAEGER_OTLP_GRPC_PORT:-4317}"

START_SERVER="${START_SERVER:-auto}"      # auto|always|never
START_JAEGER="${START_JAEGER:-auto}"      # auto|always|never
SKIP_JAEGER_CHECK="${SKIP_JAEGER_CHECK:-false}"

SERVER_STARTED_BY_SCRIPT=false
JAEGER_STARTED_BY_SCRIPT=false
SERVER_PID=""
JAEGER_CONTAINER_NAME=""

RESULT_IDS=()
RESULT_NAMES=()
RESULT_STATUS=()
RESULT_DETAILS=()

pass_count=0
fail_count=0

record_result() {
  local id="$1"
  local name="$2"
  local status="$3"
  local details="$4"

  RESULT_IDS+=("$id")
  RESULT_NAMES+=("$name")
  RESULT_STATUS+=("$status")
  RESULT_DETAILS+=("$details")

  if [[ "$status" == "PASS" ]]; then
    pass_count=$((pass_count + 1))
  else
    fail_count=$((fail_count + 1))
  fi
}

cleanup() {
  if [[ "$SERVER_STARTED_BY_SCRIPT" == true && -n "$SERVER_PID" ]]; then
    kill "$SERVER_PID" >/dev/null 2>&1 || true
    wait "$SERVER_PID" >/dev/null 2>&1 || true
  fi

  if [[ "$JAEGER_STARTED_BY_SCRIPT" == true && -n "$JAEGER_CONTAINER_NAME" ]]; then
    docker rm -f "$JAEGER_CONTAINER_NAME" >/dev/null 2>&1 || true
  fi
}

trap cleanup EXIT

curl_safe() {
  curl -sS --connect-timeout "$DEFAULT_CURL_CONNECT_TIMEOUT" --max-time "$DEFAULT_CURL_TIMEOUT" "$@"
}

http_code() {
  local url="$1"
  curl_safe -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000"
}

http_body() {
  local method="$1"
  local url="$2"
  local data="${3:-}"
  if [[ -n "$data" ]]; then
    curl_safe -X "$method" "$url" -H "Content-Type: application/json" -d "$data" 2>/dev/null || true
  else
    curl_safe -X "$method" "$url" 2>/dev/null || true
  fi
}

wait_for_url() {
  local url="$1"
  local seconds="$2"
  local i
  for ((i = 0; i < seconds; i++)); do
    if curl_safe "$url" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  return 1
}

print_header() {
  echo "=============================================================="
  echo " Chapter09 Payment Verification (CI Mode)"
  echo "=============================================================="
  echo "BASE_URL=${BASE_URL}"
  echo "API_BASE=${API_BASE}"
  echo "OPENAPI_URL=${OPENAPI_URL}"
  echo "JAEGER_URL=${JAEGER_URL}"
  echo
}

print_matrix() {
  echo
  echo "=============================================================="
  echo " PASS/FAIL Matrix"
  echo "=============================================================="
  printf "%-6s | %-42s | %-5s | %s\n" "ID" "Check" "State" "Details"
  echo "-------+--------------------------------------------+-------+-----------------------------"

  local i
  for ((i = 0; i < ${#RESULT_IDS[@]}; i++)); do
    printf "%-6s | %-42s | %-5s | %s\n" \
      "${RESULT_IDS[$i]}" "${RESULT_NAMES[$i]}" "${RESULT_STATUS[$i]}" "${RESULT_DETAILS[$i]}"
  done

  echo "-------+--------------------------------------------+-------+-----------------------------"
  echo "PASS=${pass_count} FAIL=${fail_count} TOTAL=$((pass_count + fail_count))"
  echo "=============================================================="
}

build_module() {
  if (cd "$SCRIPT_DIR" && mvn -q -DskipTests package >/tmp/payment-ci-build.log 2>&1); then
    record_result "B1" "Build payment module" "PASS" "mvn package succeeded"
  else
    record_result "B1" "Build payment module" "FAIL" "mvn package failed (see /tmp/payment-ci-build.log)"
  fi
}

ensure_server() {
  local running=false
  if curl_safe "${HEALTH_URL}" >/dev/null 2>&1; then
    running=true
  fi

  case "$START_SERVER" in
    never)
      if [[ "$running" == true ]]; then
        record_result "S1" "Server availability" "PASS" "Server already running"
      else
        record_result "S1" "Server availability" "FAIL" "Server not running and START_SERVER=never"
      fi
      ;;
    always)
      start_server
      ;;
    auto)
      if [[ "$running" == true ]]; then
        record_result "S1" "Server availability" "PASS" "Server already running"
      else
        start_server
      fi
      ;;
    *)
      record_result "S1" "Server availability" "FAIL" "Invalid START_SERVER=${START_SERVER}"
      ;;
  esac
}

start_server() {
  (cd "$SCRIPT_DIR" && mvn liberty:run >/tmp/payment-ci-server.log 2>&1) &
  SERVER_PID=$!
  SERVER_STARTED_BY_SCRIPT=true

  if wait_for_url "${HEALTH_URL}" 120; then
    record_result "S1" "Server availability" "PASS" "Started by script (pid ${SERVER_PID})"
  else
    record_result "S1" "Server availability" "FAIL" "Server did not become ready (see /tmp/payment-ci-server.log)"
  fi
}

check_endpoints() {
  local cfg
  cfg="$(http_body GET "${API_BASE}/payment-config")"
  if [[ "$cfg" == *"gateway.endpoint"* ]]; then
    record_result "E1" "GET /payment-config" "PASS" "Returned gateway.endpoint"
  else
    record_result "E1" "GET /payment-config" "FAIL" "Unexpected response: ${cfg:0:120}"
  fi

  local auth_code auth_body
  auth_code="$(curl_safe -o /dev/null -w "%{http_code}" -X POST "${API_BASE}/authorize?amount=100" 2>/dev/null || echo "000")"
  auth_body="$(http_body POST "${API_BASE}/authorize?amount=100")"
  if [[ "$auth_code" == "200" && "$auth_body" == *"status"* ]]; then
    record_result "E2" "POST /authorize?amount=100" "PASS" "HTTP 200"
  else
    record_result "E2" "POST /authorize?amount=100" "FAIL" "HTTP ${auth_code} body=${auth_body:0:120}"
  fi

  local invalid_code
  invalid_code="$(curl_safe -o /dev/null -w "%{http_code}" -X POST "${API_BASE}/authorize?amount=0" 2>/dev/null || echo "000")"
  if [[ "$invalid_code" != "200" ]]; then
    record_result "E3" "POST /authorize invalid" "PASS" "Returned non-200 (HTTP ${invalid_code})"
  else
    record_result "E3" "POST /authorize invalid" "FAIL" "Returned HTTP 200 for invalid amount"
  fi

  local payments_body
  payments_body="$(http_body POST "${API_BASE}/payments" '{"cardNumber":"4111111111111111","cardHolderName":"Test User","expiryDate":"12/25","securityCode":"123","amount":75.50}')"
  if [[ "$payments_body" == *"status"*"success"* ]]; then
    record_result "E4" "POST /payments" "PASS" "Returned success payload"
  else
    record_result "E4" "POST /payments" "FAIL" "Unexpected response: ${payments_body:0:120}"
  fi

  local verify_body
  verify_body="$(http_body POST "${API_BASE}/verify" '{"cardNumber":"4111111111111111","cardHolderName":"Test User","expiryDate":"12/25","securityCode":"123","amount":75.50}')"
  if [[ "$verify_body" == *"status"*"verified"* && "$verify_body" == *"transaction_id"* ]]; then
    record_result "E5" "POST /verify" "PASS" "Returned verified payload"
  else
    record_result "E5" "POST /verify" "FAIL" "Unexpected response: ${verify_body:0:120}"
  fi

  local health_raw health_code health_body
  health_raw="$(curl_safe -w $'\nHTTP_CODE:%{http_code}' -X GET "${API_BASE}/health/gateway" 2>/dev/null || true)"
  health_code="$(echo "$health_raw" | awk -F: '/HTTP_CODE/ {print $2}' | tr -d '[:space:]')"
  health_body="$(echo "$health_raw" | sed '/HTTP_CODE:/d')"
  health_code="${health_code:-000}"
  if { [[ "$health_code" == "200" ]] || [[ "$health_code" == "503" ]]; } && [[ "$health_body" == *"status"* ]]; then
    record_result "E6" "GET /health/gateway" "PASS" "HTTP ${health_code}"
  else
    record_result "E6" "GET /health/gateway" "FAIL" "HTTP ${health_code} body=${health_body:0:120}"
  fi

  local notify_body notify_code
  notify_code="$(curl_safe -o /dev/null -w "%{http_code}" -X POST "${API_BASE}/notify/PAY-12345?recipient=ops@example.com" 2>/dev/null || echo "000")"
  notify_body="$(http_body POST "${API_BASE}/notify/PAY-12345?recipient=ops@example.com")"
  if [[ "$notify_code" == "200" && "$notify_body" == *"queued"* ]]; then
    record_result "E7" "POST /notify/{paymentId}" "PASS" "Queued response"
  else
    record_result "E7" "POST /notify/{paymentId}" "FAIL" "HTTP ${notify_code} body=${notify_body:0:120}"
  fi
}

check_metrics_and_openapi() {
  local metrics
  metrics="$(curl_safe "$METRICS_URL" 2>/dev/null || true)"

  if [[ "$metrics" == *"http_server_request_duration"* ]]; then
    record_result "M1" "HTTP server metrics" "PASS" "http_server_request_duration present"
  else
    record_result "M1" "HTTP server metrics" "FAIL" "Missing http_server_request_duration"
  fi

  if [[ "$metrics" == *"jvm_"* || "$metrics" == *"jvm."* ]]; then
    record_result "M2" "JVM metrics" "PASS" "JVM metric family present"
  else
    record_result "M2" "JVM metrics" "FAIL" "Missing JVM metric family"
  fi

  if [[ "$metrics" == *"ft_bulkhead"*"PaymentService.processPayment"* ]]; then
    record_result "M3" "Fault tolerance metrics" "PASS" "ft_bulkhead metrics present"
  else
    record_result "M3" "Fault tolerance metrics" "FAIL" "Missing ft_bulkhead processPayment metric"
  fi

  local openapi_code openapi_alt_code openapi_effective
  openapi_code="$(http_code "$OPENAPI_URL")"
  openapi_alt_code="$(http_code "${BASE_URL}/openapi")"
  if [[ "$openapi_code" == "200" ]]; then
    openapi_effective="$OPENAPI_URL"
    record_result "O1" "OpenAPI endpoint" "PASS" "HTTP 200 at ${openapi_effective}"
  elif [[ "$openapi_alt_code" == "200" ]]; then
    openapi_effective="${BASE_URL}/openapi"
    record_result "O1" "OpenAPI endpoint" "PASS" "HTTP 200 at ${openapi_effective}"
  else
    record_result "O1" "OpenAPI endpoint" "FAIL" "HTTP ${openapi_code} at ${OPENAPI_URL}; alt /openapi=${openapi_alt_code}"
  fi
}

ensure_jaeger() {
  if [[ "$SKIP_JAEGER_CHECK" == "true" ]]; then
    record_result "T1" "Jaeger trace ingestion" "PASS" "Skipped by SKIP_JAEGER_CHECK=true"
    return
  fi

  local jaeger_running=false
  if curl_safe "${JAEGER_URL}/api/services" >/dev/null 2>&1; then
    jaeger_running=true
  fi

  case "$START_JAEGER" in
    never)
      if [[ "$jaeger_running" == false ]]; then
        record_result "T1" "Jaeger trace ingestion" "FAIL" "Jaeger not running and START_JAEGER=never"
        return
      fi
      ;;
    always)
      start_jaeger
      ;;
    auto)
      if [[ "$jaeger_running" == false ]]; then
        start_jaeger
      fi
      ;;
    *)
      record_result "T1" "Jaeger trace ingestion" "FAIL" "Invalid START_JAEGER=${START_JAEGER}"
      return
      ;;
  esac

  # Generate trace traffic
  local i
  for i in 1 2 3; do
    http_body POST "${API_BASE}/payments" '{"cardNumber":"4111111111111111","cardHolderName":"Test User","expiryDate":"12/25","securityCode":"123","amount":75.50}' >/dev/null
    sleep 1
  done

  local service_list
  local found=false
  for i in {1..20}; do
    service_list="$(curl_safe "${JAEGER_URL}/api/services" 2>/dev/null || true)"
    if [[ "$service_list" == *"payment-service"* ]]; then
      found=true
      break
    fi
    sleep 1
  done

  if [[ "$found" == true ]]; then
    record_result "T1" "Jaeger trace ingestion" "PASS" "payment-service found in Jaeger"
  else
    record_result "T1" "Jaeger trace ingestion" "FAIL" "payment-service not found in Jaeger services"
  fi
}

start_jaeger() {
  if ! command -v docker >/dev/null 2>&1; then
    record_result "T1" "Jaeger trace ingestion" "FAIL" "docker command not available"
    return
  fi

  JAEGER_CONTAINER_NAME="payment-jaeger-ci-$$"
  if docker run -d --name "$JAEGER_CONTAINER_NAME" -p 16686:16686 -p "${JAEGER_OTLP_GRPC_PORT}:4317" -p 4318:4318 jaegertracing/all-in-one:latest >/dev/null 2>&1; then
    JAEGER_STARTED_BY_SCRIPT=true
    if wait_for_url "${JAEGER_URL}/api/services" 30; then
      :
    else
      record_result "T1" "Jaeger trace ingestion" "FAIL" "Jaeger did not become ready"
    fi
  else
    # Port conflict or existing container likely. Try existing endpoint.
    if curl_safe "${JAEGER_URL}/api/services" >/dev/null 2>&1; then
      :
    else
      record_result "T1" "Jaeger trace ingestion" "FAIL" "Could not start Jaeger container"
    fi
  fi
}

main() {
  print_header
  build_module
  ensure_server

  # Only run runtime checks when server is reachable.
  if curl_safe "${HEALTH_URL}" >/dev/null 2>&1; then
    check_endpoints
    check_metrics_and_openapi
    ensure_jaeger
  else
    record_result "R1" "Runtime checks" "FAIL" "Server not reachable at ${HEALTH_URL}"
  fi

  print_matrix

  if [[ "$fail_count" -gt 0 ]]; then
    exit 1
  fi
  exit 0
}

main "$@"
