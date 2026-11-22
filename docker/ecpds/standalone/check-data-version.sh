#!/bin/bash
set -euo pipefail

data=$1
identity_file=$2
release_file=$3
marker=$data/.standalone-build-id

if [ ! -d "$data" ] || [ ! -f "$identity_file" ] || [ ! -f "$release_file" ]; then
    echo "[standalone] Data directory or image build metadata is missing." >&2
    exit 1
fi
release=$(cat "$release_file")
if [[ ! "$release" =~ ^[A-Za-z0-9._-]{1,128}$ ]]; then
    echo "[standalone] Image release metadata is invalid." >&2
    exit 1
fi
identity=$(cat "$identity_file")
if [[ ! "$identity" =~ ^[0-9a-f]{64}$ ]]; then
    echo "[standalone] Image build identity is invalid." >&2
    exit 1
fi
expected=$(printf 'build=%s\nrelease=%s' "$identity" "$release")

if [ -L "$marker" ]; then
    echo "[standalone] Data directory build marker must not be a symlink." >&2
    exit 1
fi
if [ -e "$marker" ]; then
    if [ ! -f "$marker" ] || [ "$(cat "$marker")" != "$expected" ]; then
        echo "[standalone] Data directory was not created by this standalone image build." >&2
        echo "[standalone] Restore the matching image or map a different empty data directory; no data was removed." >&2
        exit 1
    fi
    exit 0
fi
if [ -n "$(find "$data" -mindepth 1 -maxdepth 1 -print -quit)" ]; then
    echo "[standalone] Data directory is not empty and has no standalone build marker." >&2
    echo "[standalone] Restore its matching image or map a different empty data directory; no data was removed." >&2
    exit 1
fi
umask 077
temporary=$(mktemp "$data/.standalone-build-id.XXXXXXXX")
trap 'rm -f "$temporary"' EXIT
printf '%s\n' "$expected" > "$temporary"
sync -f "$temporary"
if ! ln "$temporary" "$marker"; then
    echo "[standalone] Cannot create the data directory build marker; check for concurrent startup." >&2
    exit 1
fi
sync -f "$data"
