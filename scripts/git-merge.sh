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
gh pr merge "$branch" --auto --merge --match-head-commit "$head"
