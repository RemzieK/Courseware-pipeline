#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
STATE="$HOME/deploy-state"
C="docker compose -f deploy-demo/compose.yml --env-file $STATE/current.env"
EXPECTED="$(grep '^IMAGE_TAG=' "$STATE/current.env" | cut -d= -f2)"

echo "== 1. Seed data + backup"
$C up -d
until [ "$($C ps --format '{{.Health}}' db)" = "healthy" ]; do sleep 2; done
$C exec -T db psql -U unicloud -d unicloud -qc \
  "CREATE TABLE IF NOT EXISTS dr_check(id serial PRIMARY KEY, note text);
   INSERT INTO dr_check(note) SELECT 'row '||g FROM generate_series(1,100) g;"
BEFORE=$($C exec -T db psql -U unicloud -d unicloud -tAc "SELECT count(*) FROM dr_check")
bash scripts/backup.sh

echo "== 2. DISASTER: remove containers, DB volume and images"
$C down -v
docker image prune -af >/dev/null

echo "== 3. Recovery (timer started)"
START=$(date +%s)
bash scripts/restore.sh
for i in $(seq 1 20); do
  V=$(curl -fsS http://localhost:8080/version.txt || true)
  [ "$V" = "$EXPECTED" ] && break; sleep 3
done
END=$(date +%s)

AFTER=$($C exec -T db psql -U unicloud -d unicloud -tAc "SELECT count(*) FROM dr_check")
SECS=$((END-START))
echo "Rows before=$BEFORE after=$AFTER | version=$V | RECOVERY TIME: ${SECS}s"
[ "$BEFORE" = "$AFTER" ] && [ "$V" = "$EXPECTED" ] && [ "$SECS" -le 300 ] \
  && echo "PASS (<= 300 s)" || { echo "FAIL"; exit 1; }