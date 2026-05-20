#!/bin/bash
# 确保 psql 可用，不可用时自动安装
set -euo pipefail

ensure_psql() {
  if command -v psql &>/dev/null; then
    return 0
  fi
  echo "psql not found, installing postgresql-client..."
  apt-get update -qq && apt-get install -y -qq postgresql-client >/dev/null 2>&1
  echo "postgresql-client installed."
}

ensure_psql
