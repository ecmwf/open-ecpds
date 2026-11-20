#!/bin/bash
set -euo pipefail

if [[ $# -gt 1 ]]; then
    echo "Usage: $0 [TOKEN_FILE]" >&2
    exit 2
fi

token_file=${1:-openbao/secrets/master-token}
if [[ -e "$token_file" ]]; then
    echo "Token file already exists; refusing to overwrite it." >&2
    exit 1
fi
mkdir -p "$(dirname "$token_file")"
chmod 0700 "$(dirname "$token_file")"

IFS= read -r -s -p 'Master application token: ' token
printf '\n' >&2
if [[ -z "$token" || "$token" == *[[:space:]]* ]]; then
    echo "Invalid token input." >&2
    exit 1
fi
umask 077
printf '%s\n' "$token" > "$token_file"
unset token
chmod 0600 "$token_file"
echo "Saved a 0600 Master token file at $token_file"
