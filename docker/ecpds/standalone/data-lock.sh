#!/bin/bash

lock=/data/.standalone.lock
if [ -L "$lock" ] || { [ -e "$lock" ] && [ ! -f "$lock" ]; }; then
    echo "[standalone] Data directory lock must be a regular file, not a symlink." >&2
    exit 1
fi
previous_umask=$(umask)
umask 077
exec 9>>"$lock"
umask "$previous_umask"
unset previous_umask
if ! flock -n 9; then
    echo "[standalone] Data directory is already in use by another standalone container." >&2
    exit 1
fi
chmod 0600 "$lock"
