#!/usr/bin/env bash
# Създава закачената задача „Правила за code review“ и я добавя в борда.
# Употреба (в Git Bash, в папката с REVIEW_RULES_issue.md):
#   bash create_review_issue.sh
set -euo pipefail
REPO=RemzieK/Courseware-pipeline
DIR="$(cd "$(dirname "$0")" && pwd)"

gh label create process --repo "$REPO" --color 5319E7 --description "Правила за работа в екипа" --force >/dev/null

url=$(gh issue create --repo "$REPO" \
  --title "[ВСИЧКИ] Правила за code review – кой кого рецензира" \
  --body-file "$DIR/REVIEW_RULES_issue.md" \
  --label process,P0 \
  --assignee RadostinPavlov,RemzieK,hadelmisljimi,Zlatin44TU,danielgeorgiev10,stodorichkov)
echo "$url"

gh issue pin "$url" --repo "$REPO"
gh project item-add 1 --owner RadostinPavlov --url "$url" >/dev/null
echo "Готово: задачата е закачена най-горе в Issues и е добавена в борда."
