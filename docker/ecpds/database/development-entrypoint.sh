#!/bin/bash
set -euo pipefail
set +x
install -d -o mysql -g mysql -m 0700 /run/ecpds-development-private
install -o mysql -g mysql -m 0600 /run/ecpds-development/database-password \
    /run/ecpds-development-private/database-password
exec /usr/local/bin/docker-entrypoint.sh "$@"
