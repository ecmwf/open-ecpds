#!/bin/sh
set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "Run the operator helper as container root; the OpenBao server uses UID 100." >&2
    exit 1
fi

BAO_ADDR=${BAO_ADDR:-https://openbao:8200}
BAO_CACERT=${BAO_CACERT:-/openbao/tls/ca.crt}
export BAO_ADDR BAO_CACERT

printf 'Initial OpenBao root token to revoke: ' >&2
IFS= read -r -s BAO_TOKEN </dev/tty
printf '\n' >&2
if [ -z "$BAO_TOKEN" ]; then
    echo "A root token is required." >&2
    exit 1
fi
export BAO_TOKEN

/usr/bin/bao token revoke -self >/dev/null
echo "Initial root token revoked."
