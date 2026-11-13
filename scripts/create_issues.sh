#!/usr/bin/env bash
# Създава етикетите и issues за Седмица 1 в GitHub и (по желание) ги добавя в GitHub Project.
#
# Изисква: GitHub CLI (gh), влязъл с: gh auth login
#          за добавяне в Project:    gh auth refresh -s project
#
# Употреба:
#   ./create_issues.sh OWNER/REPO                    # само issues
#   ./create_issues.sh OWNER/REPO PROJECT_NUMBER     # issues + добавяне в Project (номерът е в URL-а на борда)
#   ./create_issues.sh OWNER/REPO PROJECT_NUMBER PROJECT_OWNER   # ако бордът е в друг акаунт (напр. твоя)
#
# Пусни го веднъж – при повторно пускане ще създаде дублирани issues.
set -euo pipefail

REPO="${1:?Посочи хранилище: OWNER/REPO}"
PROJECT="${2:-}"
OWNER="${3:-${REPO%%/*}}"
DIR="$(cd "$(dirname "$0")" && pwd)"
TSV="$DIR/issues.tsv"

label() { gh label create "$1" --repo "$REPO" --color "$2" --description "$3" --force >/dev/null; }

echo "Етикети..."
label P0 B60205 "Нужно за демото"
label P1 D93F0B "Важно, след P0"
label P2 FBCA04 "Ако остане време"
label role:pm 1D76DB "Project Manager"
label role:security 5319E7 "Security Engineer"
label role:devops 0E8A16 "DevOps"
label role:se1 006B75 "SE 1 – Backend"
label role:se2 0052CC "SE 2 – Frontend"
label role:se3 C5DEF5 "SE 3 – Качество и интеграции"
label req:1-repository BFDADC "Изискване 1 – хранилище"
label req:2-peaks BFDADC "Изискване 2 – пикове"
label req:3-guarantees BFDADC "Изискване 3 – гаранции"
label req:general EDEDED "Общо"
for d in 1 2 3 4 5; do label "day:$d" F9D0C4 "Ден $d"; done

echo "Issues..."
while IFS=$'\t' read -r title body labels; do
  url=$(gh issue create --repo "$REPO" --title "$title" --body "$(printf '%b' "$body")" --label "$labels")
  echo "  $url"
  if [[ -n "$PROJECT" ]]; then
    gh project item-add "$PROJECT" --owner "$OWNER" --url "$url" >/dev/null
  fi
done < "$TSV"

echo "Готово."
