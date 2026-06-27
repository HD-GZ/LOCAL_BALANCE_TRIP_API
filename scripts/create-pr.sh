#!/usr/bin/env bash

set -euo pipefail

BASE_BRANCH="develop"
REMOTE_NAME="origin"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SCHEMA_FILE="$SCRIPT_DIR/pr-ai-output.schema.json"
PR_TEMPLATE="$ROOT_DIR/.github/pull_request_template.md"

usage() {
  cat <<'USAGE'
Usage:
  scripts/create-pr.sh

Creates a GitHub PR from the current branch.
The current branch must match: feat/{issue-number}-{slug}
USAGE
}

fail() {
  echo "error: $*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "'$1' command is required"
}

json_string() {
  jq -r "$1" "$AI_OUTPUT_FILE"
}

json_bullets() {
  local query="$1"
  local fallback="${2:-}"

  if [[ "$(jq -r "$query | length" "$AI_OUTPUT_FILE")" == "0" ]]; then
    [[ -n "$fallback" ]] && printf '* %s\n' "$fallback"
    return
  fi

  jq -r "$query[] | \"* \" + ." "$AI_OUTPUT_FILE"
}

cleanup() {
  rm -f "${AI_OUTPUT_FILE:-}" "${PR_BODY_FILE:-}" "${DIFF_FILE:-}" "${PROMPT_FILE:-}"
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ "$#" -ne 0 ]]; then
  usage
  exit 1
fi

cd "$ROOT_DIR"

require_command git
require_command gh
require_command jq
require_command codex

[[ -f "$SCHEMA_FILE" ]] || fail "missing schema file: $SCHEMA_FILE"
[[ -f "$PR_TEMPLATE" ]] || fail "missing PR template: $PR_TEMPLATE"

gh auth status >/dev/null 2>&1 || fail "GitHub CLI is not authenticated. Run: gh auth login -h github.com"

if [[ -n "$(git status --porcelain)" ]]; then
  fail "working tree must be clean before creating a PR"
fi

git rev-parse --verify "$BASE_BRANCH" >/dev/null 2>&1 || fail "base branch '$BASE_BRANCH' does not exist locally"

CURRENT_BRANCH="$(git branch --show-current)"
[[ -n "$CURRENT_BRANCH" ]] || fail "could not determine current branch"
[[ "$CURRENT_BRANCH" != "$BASE_BRANCH" ]] || fail "cannot create a PR from '$BASE_BRANCH'"

if [[ ! "$CURRENT_BRANCH" =~ ^feat/([0-9]+)-[a-z0-9]+(-[a-z0-9]+)*$ ]]; then
  fail "current branch must match 'feat/{issue-number}-{slug}': $CURRENT_BRANCH"
fi

ISSUE_NUMBER="${BASH_REMATCH[1]}"

if [[ -z "$(git log --oneline "$BASE_BRANCH..HEAD")" ]]; then
  fail "current branch has no commits ahead of '$BASE_BRANCH'"
fi

AI_OUTPUT_FILE="$(mktemp)"
PR_BODY_FILE="$(mktemp)"
DIFF_FILE="$(mktemp)"
PROMPT_FILE="$(mktemp)"
trap cleanup EXIT

{
  echo "Current branch: $CURRENT_BRANCH"
  echo
  echo "Commit log:"
  git log --oneline "$BASE_BRANCH..HEAD"
  echo
  echo "Diff stat:"
  git diff --stat "$BASE_BRANCH...HEAD"
  echo
  echo "Diff:"
  git diff --find-renames "$BASE_BRANCH...HEAD"
} > "$DIFF_FILE"

cat > "$PROMPT_FILE" <<PROMPT_EOF
Create GitHub pull request metadata by summarizing this branch.

Return only JSON matching the provided schema.

Rules:
- title: concise Korean PR title using conventional commit style, such as "feat: 이슈 브랜치 생성 자동화 추가".
- overview: Korean summary of what this PR does in one or two sentences.
- changes: Korean bullet item texts for the main implementation changes.
- notes: Korean bullet item texts for review notes, limitations, or operational notes. Use an empty array if there is nothing meaningful.
- Do not include markdown fences or explanatory text.

Repository context:
Base branch: $BASE_BRANCH
Head branch: $CURRENT_BRANCH
Linked issue: #$ISSUE_NUMBER

Branch changes:
$(cat "$DIFF_FILE")
PROMPT_EOF

echo "Generating PR metadata with Codex..."
codex exec \
  --sandbox read-only \
  --ask-for-approval never \
  --ephemeral \
  --output-schema "$SCHEMA_FILE" \
  --output-last-message "$AI_OUTPUT_FILE" \
  - < "$PROMPT_FILE" >/dev/null

jq -e '
  type == "object"
  and (.title | type == "string" and length > 0)
  and (.overview | type == "string" and length > 0)
  and (.changes | type == "array" and length > 0 and all(.[]; type == "string" and length > 0))
  and (.notes | type == "array" and all(.[]; type == "string" and length > 0))
' "$AI_OUTPUT_FILE" >/dev/null || fail "Codex returned invalid metadata: $(cat "$AI_OUTPUT_FILE")"

TITLE="$(json_string '.title')"
OVERVIEW="$(json_string '.overview')"
CHANGES="$(json_bullets '.changes')"
NOTES="$(json_bullets '.notes' '없음')"
CHECKLIST="$(awk '/^### ✅/{flag=1} flag{print}' "$PR_TEMPLATE")"

cat > "$PR_BODY_FILE" <<BODY_EOF
### 🔍 개요

* $OVERVIEW

- close #$ISSUE_NUMBER

---

### 🚀 주요 변경 내용

$CHANGES


---

### 💬 참고 사항

$NOTES


---

$CHECKLIST
BODY_EOF

cat <<CONFIRM_EOF

Generated PR metadata:
  title: $TITLE

PR body:
$(cat "$PR_BODY_FILE")

CONFIRM_EOF

read -r -p "Push branch and create PR? [y/N] " CONFIRM
case "$CONFIRM" in
  y|Y|yes|YES) ;;
  *) echo "Canceled."; exit 0 ;;
esac

echo "Pushing $CURRENT_BRANCH..."
git push -u "$REMOTE_NAME" "$CURRENT_BRANCH"

echo "Creating GitHub PR..."
PR_URL="$(gh pr create \
  --base "$BASE_BRANCH" \
  --head "$CURRENT_BRANCH" \
  --title "$TITLE" \
  --body-file "$PR_BODY_FILE")"

cat <<DONE_EOF

Created PR: $PR_URL
DONE_EOF
