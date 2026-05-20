#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Seeding data (starting application)..."
cd "$SCRIPT_DIR/.." && mvn spring-boot:run
echo "Seed complete."
