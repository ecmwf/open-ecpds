#!/bin/bash
set -euo pipefail
set +x

root=$(cd "$(dirname "$0")/.." && pwd)
directory=$root/run/etc/ecpds/master
file=$directory/development-database-password
data=$root/run/var/lib/ecpds/database
mkdir -p "$directory"
if [ -L "$file" ] || { [ -e "$file" ] && [ ! -f "$file" ]; }; then
    echo "Development database password must be a regular file." >&2
    exit 1
fi

existing=false
if [ -d "$data/mysql" ] || [ -d "$data/ecpds" ]; then
    existing=true
fi
if [ -f "$file" ] && [ -s "$file" ] && { [ "$existing" = true ] || [ -z "${ECPDS_DATABASE_PASSWORD:-}" ]; }; then
    chmod 0600 "$file"
    exit 0
fi
if [ -e "$file" ] && [ ! -s "$file" ]; then
    echo "Development database password file is empty; restore it or remove it before retrying." >&2
    exit 1
fi

password=${ECPDS_DATABASE_PASSWORD:-}
if [ -z "$password" ] && [ "$existing" = true ]; then
    if ! { : < /dev/tty; } 2>/dev/null; then
        echo "Existing database detected; set ECPDS_DATABASE_PASSWORD to its current password and retry." >&2
        exit 1
    fi
    printf 'Existing development database master password: ' > /dev/tty
    IFS= read -r -s password < /dev/tty || exit 1
    printf '\n' > /dev/tty
elif [ -z "$password" ]; then
    password=$(openssl rand -hex 32)
fi
if [ -z "$password" ] || [[ "$password" == *$'\n'* || "$password" == *$'\r'* ]]; then
    echo "Development database password must be a nonempty single line." >&2
    exit 1
fi
umask 077
temporary=$(mktemp "$directory/.development-database-password.XXXXXXXX")
trap 'rm -f "$temporary"' EXIT
printf '%s' "$password" > "$temporary"
unset password
if [ "$(id -u)" = 0 ]; then
    owner=$(stat -c '%u:%g' "$root" 2>/dev/null || stat -f '%u:%g' "$root")
    chown "$owner" "$temporary"
fi
if [ -e "$file" ]; then
    mv -f "$temporary" "$file"
else
    if ! ln "$temporary" "$file"; then
        echo "Cannot create development database password file." >&2
        exit 1
    fi
fi
