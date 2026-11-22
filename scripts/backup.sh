#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
STATE="$HOME/deploy-state"; BK="$HOME/backups"; mkdir -p "$BK"
FILE="$BK/unicloud-$(date -u +%Y%m%dT%H%M%SZ).sql.gz"
docker compose -f deploy-demo/compose.yml --env-file "$STATE/current.env" \
  exec -T db pg_dump -U unicloud --clean --if-exists unicloud | gzip > "$FILE"
ls -t "$BK"/*.sql.gz | tail -n +8 | xargs -r rm --   # keep last 7
echo "Backup: $FILE"