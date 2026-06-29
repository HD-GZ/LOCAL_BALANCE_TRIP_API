#!/usr/bin/env bash

set -euo pipefail

BASE_BRANCH="develop"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SCHEMA_FILE="$SCRIPT_DIR/issue-ai-output.schema.json"
ISSUE_TEMPLATE="$ROOT_DIR/.github/ISSUE_TEMPLATE/default_issue.md"

usage() {
  cat <<'USAGE'
Usage:
  scripts/create-issue-branch.sh "작업명"

Example:
  scripts/create-issue-branch.sh "게시글 알림 구독 endpoint 추가"
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

cleanup() {
  rm -f "${AI_OUTPUT_FILE:-}" "${ISSUE_BODY_FILE:-}"
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ "$#" -ne 1 ]]; then
  usage
  exit 1
fi

WORK_NAME="$1"
[[ -n "$WORK_NAME" ]] || fail "work name must not be empty"

cd "$ROOT_DIR"

require_command git
require_command gh
require_command jq
require_command codex

[[ -f "$SCHEMA_FILE" ]] || fail "missing schema file: $SCHEMA_FILE"
[[ -f "$ISSUE_TEMPLATE" ]] || fail "missing issue template: $ISSUE_TEMPLATE"

gh auth status >/dev/null 2>&1 || fail "GitHub CLI is not authenticated. Run: gh auth login -h github.com"

if [[ -n "$(git status --porcelain)" ]]; then
  fail "working tree must be clean before creating and switching branches"
fi

git rev-parse --verify "$BASE_BRANCH" >/dev/null 2>&1 || fail "base branch '$BASE_BRANCH' does not exist locally"

AI_OUTPUT_FILE="$(mktemp)"
ISSUE_BODY_FILE="$(mktemp)"
trap cleanup EXIT

PROMPT=$(cat <<PROMPT_EOF
Create GitHub issue metadata from this work name.

Work name:
$WORK_NAME

Return only JSON matching the provided schema.

Rules:
- title: a concise Korean GitHub issue title.
- slug: ASCII kebab-case for a git branch, lowercase only.
- slug should translate or extract English keywords from Korean.
- summary: Korean issue overview in one or two practical sentences.
- Do not include markdown fences or explanatory text.
PROMPT_EOF
)

echo "Generating issue metadata with Codex..."
codex exec \
  --sandbox read-only \
  --ephemeral \
  --output-schema "$SCHEMA_FILE" \
  --output-last-message "$AI_OUTPUT_FILE" \
  "$PROMPT" >/dev/null

jq -e '
  type == "object"
  and (.title | type == "string" and length > 0)
  and (.slug | type == "string" and test("^[a-z0-9]+(-[a-z0-9]+)*$"))
  and (.summary | type == "string" and length > 0)
' "$AI_OUTPUT_FILE" >/dev/null || fail "Codex returned invalid metadata: $(cat "$AI_OUTPUT_FILE")"

TITLE="$(json_string '.title')"
SLUG="$(json_string '.slug')"
SUMMARY="$(json_string '.summary')"

cat <<CONFIRM_EOF

Generated issue metadata:
  title:   $TITLE
  slug:    $SLUG
  summary: $SUMMARY

CONFIRM_EOF

read -r -p "Create issue and switch branch? [y/N] " CONFIRM
case "$CONFIRM" in
  y|Y|yes|YES) ;;
  *) echo "Canceled."; exit 0 ;;
esac

awk -v summary="$SUMMARY" '
  NR == 1 && $0 == "---" {
    in_frontmatter = 1
    next
  }
  in_frontmatter && $0 == "---" {
    in_frontmatter = 0
    next
  }
  in_frontmatter {
    next
  }
  !replaced && $0 == "*" {
    print "* " summary
    replaced = 1
    next
  }
  { print }
' "$ISSUE_TEMPLATE" > "$ISSUE_BODY_FILE"

echo "Creating GitHub issue..."
ISSUE_URL="$(gh issue create --title "$TITLE" --body-file "$ISSUE_BODY_FILE")"
ISSUE_NUMBER="${ISSUE_URL##*/}"

[[ "$ISSUE_NUMBER" =~ ^[0-9]+$ ]] || fail "could not parse issue number from gh output: $ISSUE_URL"

BRANCH_NAME="feat/${ISSUE_NUMBER}-${SLUG}"
[[ "$BRANCH_NAME" =~ ^feat/[0-9]+-[a-z0-9]+(-[a-z0-9]+)*$ ]] || fail "invalid branch name: $BRANCH_NAME"

if git rev-parse --verify "$BRANCH_NAME" >/dev/null 2>&1; then
  fail "branch already exists locally: $BRANCH_NAME"
fi

echo "Switching to $BASE_BRANCH..."
git switch "$BASE_BRANCH"

echo "Creating branch $BRANCH_NAME..."
git switch -c "$BRANCH_NAME"

cat <<DONE_EOF

Created issue:  #$ISSUE_NUMBER
Issue URL:      $ISSUE_URL
Current branch: $BRANCH_NAME
DONE_EOF
