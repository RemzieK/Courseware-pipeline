#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

STATE="$HOME/deploy-state"
mkdir -p "$STATE"

TAG="${1:?usage: deploy.sh <tag> [owner]}"
OWNER="${2:-$(grep '^OWNER=' "$STATE/current.env" | cut -d= -f2)}"

printf 'IMAGE_TAG=%s\nOWNER=%s\n' "$TAG" "$OWNER" > "$STATE/current.env"
echo "$TAG" >> "$STATE/history"

docker compose -f deploy-demo/compose.yml --env-file "$STATE/current.env" up -d --remove-orphans
echo "Deployed $TAG"