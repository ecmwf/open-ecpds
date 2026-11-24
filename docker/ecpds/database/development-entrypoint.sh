#!/bin/bash
set -euo pipefail
set +x
if [ -n "${ECPDS_DATABASE_PASSWORD_FILE:-}" ]; then
    password=$(cat "$ECPDS_DATABASE_PASSWORD_FILE")
elif [ -n "${ECPDS_DATABASE_PASSWORD:-}" ]; then
    password=$ECPDS_DATABASE_PASSWORD
elif [ -f /run/ecpds-development/database-password ]; then
    password=$(cat /run/ecpds-development/database-password)
else
    echo "Set ECPDS_DATABASE_PASSWORD_FILE or ECPDS_DATABASE_PASSWORD, or mount the development database password." >&2
    exit 1
fi
if [ -z "$password" ] || [[ "$password" == *$'\n'* || "$password" == *$'\r'* ]]; then
    echo "The application database password must be a nonempty single line." >&2
    exit 1
fi
install -d -o mysql -g mysql -m 0700 /run/ecpds-development-private
install -o mysql -g mysql -m 0600 /dev/null /run/ecpds-development-private/database-password
printf '%s\n' "$password" > /run/ecpds-development-private/database-password
unset password ECPDS_DATABASE_PASSWORD
exec /usr/local/bin/docker-entrypoint.sh "$@"
