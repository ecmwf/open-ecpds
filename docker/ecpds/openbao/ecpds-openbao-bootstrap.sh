#!/bin/sh
set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "Run the operator helper as container root; the OpenBao server uses UID 100." >&2
    exit 1
fi

BAO_ADDR=${BAO_ADDR:-https://openbao:8200}
BAO_CACERT=${BAO_CACERT:-/openbao/tls/ca.crt}
export BAO_ADDR BAO_CACERT

printf 'OpenBao initial root token: ' >&2
IFS= read -r -s BAO_TOKEN </dev/tty
printf '\n' >&2
if [ -z "$BAO_TOKEN" ]; then
    echo "A root token is required." >&2
    exit 1
fi
export BAO_TOKEN

if ! /usr/bin/bao status >/dev/null 2>&1; then
    echo "OpenBao is sealed, uninitialized, or unavailable; check status and unseal first." >&2
    exit 1
fi

if ! /usr/bin/bao secrets list -format=json | grep -q '"ecpds/"'; then
    /usr/bin/bao secrets enable -path=ecpds kv-v2
fi
/usr/bin/bao policy write ecpds-master /openbao/policies/master-policy.hcl >/dev/null
echo "KV v2 mount and least-privilege Master policy are ready."
