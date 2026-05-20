#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/_lib.sh"

PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$PROJECT_DIR/.env"
if [ -f "$ENV_FILE" ]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

: "${POSTGRES_HOST:=localhost}"
: "${POSTGRES_PORT:=5432}"
: "${POSTGRES_USER:=weixin}"
: "${POSTGRES_PASSWORD:=weixin123}"
: "${POSTGRES_DB_NAME:=weixin_library}"

export PGPASSWORD="$POSTGRES_PASSWORD"

echo "Creating database \"$POSTGRES_DB_NAME\"..."

CREATE_OUTPUT=$(psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres \
  -c "CREATE DATABASE \"$POSTGRES_DB_NAME\"" 2>&1) || true

if echo "$CREATE_OUTPUT" | grep -q "already exists"; then
  echo "Database \"$POSTGRES_DB_NAME\" already exists, skipping."
elif echo "$CREATE_OUTPUT" | grep -q "CREATE DATABASE"; then
  echo "Database \"$POSTGRES_DB_NAME\" created."
else
  echo "$CREATE_OUTPUT"
  exit 1
fi
