#!/bin/bash
set -euo pipefail

container=${OPENBAO_CONTAINER:-standalone-openbao-vault}
command=${1:-}

case "$command" in
    status)
        exec docker exec "$container" /usr/bin/bao status
        ;;
    init)
        exec docker exec "$container" /usr/bin/bao operator init -format=json \
            -key-shares=5 -key-threshold=3
        ;;
    unseal)
        exec docker exec -it "$container" /usr/bin/bao operator unseal
        ;;
    bootstrap)
        exec docker exec -it --user 0:0 "$container" /usr/local/bin/ecpds-openbao-bootstrap.sh
        ;;
    issue-token)
        exec docker exec -it --user 0:0 "$container" /usr/local/bin/ecpds-openbao-issue-token.sh
        ;;
    revoke-root)
        exec docker exec -it --user 0:0 "$container" /usr/local/bin/ecpds-openbao-revoke-root.sh
        ;;
    *)
        echo "Usage: $0 {status|init|unseal|bootstrap|issue-token|revoke-root}" >&2
        exit 2
        ;;
esac
