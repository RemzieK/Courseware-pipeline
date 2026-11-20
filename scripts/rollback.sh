#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

STATE="$HOME/deploy-state"
if [ "$(wc -l < "$STATE/history")" -lt 2 ]; then
  echo "Nothing to roll back to"; exit 1
fi

sed -i '$d' "$STATE/history"          # drop the current version
TARGET="$(tail -n 1 "$STATE/history")"
OWNER="$(grep '^OWNER=' "$STATE/current.env" | cut -d= -f2)"

printf 'IMAGE_TAG=%s\nOWNER=%s\n' "$TARGET" "$OWNER" > "$STATE/current.env"
docker compose -f deploy-demo/compose.yml --env-file "$STATE/current.env" up -d
echo "Rolled back to $TARGET"