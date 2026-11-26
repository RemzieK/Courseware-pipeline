#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
STATE="$HOME/deploy-state"; BK="$HOME/backups"
FILE="${1:-$(ls -t "$BK"/*.sql.gz | head -n 1)}"
C="docker compose -f deploy-demo/compose.yml --env-file $STATE/current.env"
$C up -d db
until [ "$($C ps --format '{{.Health}}' db)" = "healthy" ]; do sleep 2; done
gunzip -c "$FILE" | $C exec -T db psql -U unicloud -d unicloud -v ON_ERROR_STOP=1 -q
$C up -d
echo "Restored from $FILE"