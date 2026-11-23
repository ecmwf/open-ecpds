#!/bin/bash
set +x
password=$(cat /run/ecpds-development-private/database-password)
if [ -z "$password" ] || [[ "$password" == *$'\n'* || "$password" == *$'\r'* ]]; then
    echo "Invalid development database password." >&2
    exit 1
fi
escaped=${password//\'/\'\'}
unset password
printf "SET SESSION sql_mode='NO_BACKSLASH_ESCAPES';\nALTER USER 'master'@'%%' IDENTIFIED BY '%s';\n" \
    "$escaped" | docker_process_sql
unset escaped
