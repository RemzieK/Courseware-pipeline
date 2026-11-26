#!/usr/bin/env bash
# Създава задачите за Седмица 2 в GitHub, задава ги на отговорниците и ги добавя в борда.
#
# Изисква: GitHub CLI (gh), влязъл с gh auth login и gh auth refresh -s project
#
# Употреба (в Git Bash, от папката на проекта):
#   ./scripts/create_issues_week2.sh
#
# Пусни го само веднъж – повторно пускане създава дублирани задачи.
set -euo pipefail

REPO="RemzieK/Courseware-pipeline"
PROJECT="1"
PROJECT_OWNER="RadostinPavlov"
DIR="$(cd "$(dirname "$0")" && pwd)"
TSV="$DIR/issues_week2.tsv"

if gh issue list --repo "$REPO" --search "UC-48 in:title" --state all --json number --jq length | grep -qv '^0$'; then
  echo "Задачите за Седмица 2 вече съществуват (има UC-48). Спирам, за да няма дубликати."
  exit 1
fi

label() { gh label create "$1" --repo "$REPO" --color "$2" --description "$3" --force >/dev/null; }

echo "Етикети..."
label week:2 0E8A16 "Седмица 2"
label w2:1-storage-cdn BFDADC "Седм. 2, изискване 1 – съхранение и CDN"
label w2:2-processing BFDADC "Седм. 2, изискване 2 – обработка при поискване"
label w2:3-integration BFDADC "Седм. 2, изискване 3 – интеграция"
# P0/P1, role:*, req:general и day:1..5 вече съществуват от Седмица 1.

echo "Задачи..."
while IFS=$'\t' read -r title body labels assignee; do
  url=$(gh issue create --repo "$REPO" --title "$title" --body "$(printf '%b' "$body")" --label "$labels" --assignee "$assignee")
  echo "  $url  -> $assignee"
  gh project item-add "$PROJECT" --owner "$PROJECT_OWNER" --url "$url" >/dev/null
done < "$TSV"

echo "Готово."
