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
: "${APP_ENV:=development}"

# 非开发环境需要显式确认
if [ "$APP_ENV" != "development" ] && [ "${DISABLE_DATABASE_ENVIRONMENT_CHECK:-}" != "1" ]; then
  echo "Error: APP_ENV is not development."
  echo "If you want to drop the database, set DISABLE_DATABASE_ENVIRONMENT_CHECK=1"
  exit 1
fi

export PGPASSWORD="$POSTGRES_PASSWORD"

echo "Dropping database \"$POSTGRES_DB_NAME\"..."
psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres \
  -c "DROP DATABASE IF EXISTS \"$POSTGRES_DB_NAME\""
echo "Database \"$POSTGRES_DB_NAME\" dropped."
