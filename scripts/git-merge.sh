#!/usr/bin/env bash
set -euo pipefail

branch="$(git symbolic-ref --quiet --short HEAD)" || {
  printf 'Error: Switch to the pull request branch first.\n' >&2
  exit 1
}
if [[ "$branch" == master ]]; then
  printf 'Error: Switch to the pull request branch first; master is not a PR branch.\n' >&2
  exit 1
fi

head="$(git rev-parse --verify HEAD)"
auto_merge="$(gh api 'repos/{owner}/{repo}' --jq '.allow_auto_merge')" || {
  printf 'Error: Cannot determine whether the repository permits auto-merge.\n' >&2
  exit 1
}

case "$auto_merge" in
  true)
    gh pr merge "$branch" --auto --merge --match-head-commit "$head"
    ;;
  false)
    printf 'Auto-merge is disabled; attempting a normal merge without bypassing protections.\n'
    if ! gh pr merge "$branch" --merge --match-head-commit "$head"; then
      printf 'Merge not completed. Resolve required reviews and checks, then retry; alternatively, enable auto-merge in repository settings.\n' >&2
      exit 1
    fi
    ;;
  *)
    printf 'Error: Unexpected auto-merge setting: %s\n' "$auto_merge" >&2
    exit 1
    ;;
esac
