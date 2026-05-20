#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "========== Database Reset =========="

echo ""
echo ">>> Step 1: Drop database..."
bash "$SCRIPT_DIR/db-drop.sh"

echo ""
echo ">>> Step 2: Create database..."
bash "$SCRIPT_DIR/db-create.sh"

echo ""
echo ">>> Step 3: Seed data (starting application)..."
cd "$SCRIPT_DIR/.." && mvn spring-boot:run

echo ""
echo "========== Database reset complete =========="
