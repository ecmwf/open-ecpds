#!/usr/bin/env bash
set -euo pipefail

fail() {
  printf 'Error: %s\n' "$*" >&2
  exit 1
}

for command in git gh; do
  command -v "$command" >/dev/null 2>&1 || fail "$command is required"
done

root="$(git rev-parse --show-toplevel)" || fail 'Run this in the Git repository.'
cd "$root"
current="$(git symbolic-ref --quiet --short HEAD)" || fail 'Detached HEAD is not supported.'
branch="${GIT_PUBLISH_BRANCH:-}"
message="${GIT_PUBLISH_MESSAGE:-Update OpenECPDS}"
if [[ -z "$branch" ]]; then
  if [[ "$current" == master ]]; then
    branch="update-$(date -u +%Y%m%d-%H%M%S)"
  else
    branch="$current"
  fi
fi
validated_branch="$(git check-ref-format --branch "$branch")" || fail 'Invalid branch name.'
[[ "$validated_branch" == "$branch" ]] || fail 'Use a literal branch name.'
[[ "$branch" != master ]] || fail 'The pull request branch cannot be master.'
[[ "$current" == master || "$current" == "$branch" ]] ||
  fail "Switch to master or the requested branch before publishing."

github_repo() {
  local url="$1" repo
  case "$url" in
    git@github.com:*) repo="${url#git@github.com:}" ;;
    ssh://git@github.com/*) repo="${url#ssh://git@github.com/}" ;;
    https://github.com/*) repo="${url#https://github.com/}" ;;
    *) return 1 ;;
  esac
  repo="${repo%.git}"
  [[ "$repo" =~ ^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$ ]] || return 1
  printf '%s' "$repo"
}

fetch_url="$(git remote get-url origin)" || fail 'Configure an origin remote.'
push_url="$(git remote get-url --push --all origin)" || fail 'Configure an origin push URL.'
repo="$(github_repo "$fetch_url")" || fail 'Origin must be a GitHub.com SSH or HTTPS repository URL without embedded credentials.'
push_repo="$(github_repo "$push_url")" || fail 'Origin must have one GitHub.com push URL without embedded credentials.'
[[ "${repo,,}" == "${push_repo,,}" ]] || fail 'Origin fetch and push URLs must refer to the same repository.'
owner="${repo%%/*}"

if ! gh auth status --hostname github.com --active; then
  fail 'GitHub CLI authentication failed. Check GH_TOKEN/GITHUB_TOKEN or run gh auth login.'
fi

git fetch --no-tags origin refs/heads/master:refs/remotes/origin/master
git merge-base --is-ancestor refs/remotes/origin/master HEAD ||
  fail 'Current history is behind or diverged from origin/master; resolve it before publishing.'

if [[ "$current" == master ]]; then
  if git show-ref --verify --quiet "refs/heads/$branch"; then
    fail 'The branch already exists locally; switch to it to resume.'
  fi
  remote_branch="$(git ls-remote --heads origin "refs/heads/$branch")" ||
    fail 'Cannot inspect the remote branch.'
  [[ -z "$remote_branch" ]] || fail 'The branch already exists on origin; choose another branch.'
fi

changes="$(git status --porcelain --untracked-files=normal)"
if [[ -z "$changes" && "$(git rev-list --count refs/remotes/origin/master..HEAD)" == 0 ]]; then
  fail 'There are no changes or commits to publish.'
fi

existing_pr="$(gh api --method GET "repos/$repo/pulls" \
  -f state=open -f "head=$owner:$branch" -f base=master \
  --jq '.[0].html_url // empty')" || fail 'Cannot inspect pull requests.'

if [[ "$current" == master ]]; then
  git switch -c "$branch"
fi
if [[ -n "$changes" ]]; then
  printf 'Staging all non-ignored changes, including untracked files.\n'
  git add --all
  git diff --cached --quiet || git commit -m "$message"
fi
if [[ "$(git rev-list --count refs/remotes/origin/master..HEAD)" == 0 ]]; then
  fail 'There are no commits to publish.'
fi

printf '\nCommits to publish:\n'
git --no-pager log --oneline refs/remotes/origin/master..HEAD
git push -u origin "refs/heads/$branch:refs/heads/$branch"
if [[ -n "$existing_pr" ]]; then
  printf 'Existing pull request: %s\n' "$existing_pr"
else
  gh pr create --repo "$repo" --base master --head "$branch" --fill
fi
printf 'Pull request was not merged. Review its checks and approvals before merging.\n'
printf 'Tip: run make git-merge to merge, or enable auto-merge if the repository permits it.\n'
