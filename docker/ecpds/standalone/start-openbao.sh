#!/bin/bash
set -euo pipefail

mode=${ECPDS_OPENBAO_ENABLED:-embedded}
if [ "$mode" != embedded ] && [ "$mode" != manual ] && [ "$mode" != auto ]; then
    exit 0
fi

export BAO_ADDR=https://127.0.0.1:8200
export BAO_CACERT=/data/lib/openbao/tls/server.crt
operator=/data/lib/openbao-operator
init=$operator/init.json
token=/data/lib/master/openbao/token
ready=/run/ecpds/openbao/ready
umask 077
rm -f "$ready"

seal_config=()
if [ "$mode" = auto ]; then
    seal_config=(-config=/run/ecpds/openbao-seal/config.hcl)
fi
runuser -u ecpds-openbao -- env -i HOME=/data/lib/openbao PATH=/usr/bin:/bin \
    /usr/local/bin/bao server -config=/etc/ecpds/openbao.hcl "${seal_config[@]}" &
server=$!
cleanup() {
    rm -f "$ready"
    kill "$server" 2>/dev/null || true
    wait "$server" 2>/dev/null || true
}
trap cleanup EXIT
trap 'exit 0' TERM INT

health() {
    curl --silent --show-error --max-time 5 --cacert "$BAO_CACERT" "$BAO_ADDR/v1/sys/health" 2>/dev/null
}

renew_master_token() {
    if [ -s "$token" ] && BAO_TOKEN="$(<"$token")" /usr/local/bin/bao token renew >/dev/null 2>&1; then
        return 0
    fi
    local credentials=/run/secrets/ecpds-openbao-approle.json temporary
    [ "$mode" = auto ] && [ -f "$credentials" ] && [ ! -L "$credentials" ] || return 1
    [ "$(stat -c '%u' "$credentials")" = 12001 ] &&
        [ "$(stat -c '%a' "$credentials")" = 400 ] || return 1
    temporary=$(mktemp /run/ecpds/openbao-approle.XXXXXXXX) || return 1
    if ! jq -ec 'select(.role_id | type == "string" and length > 0) |
                 select(.secret_id | type == "string" and length > 0) |
                 {role_id, secret_id}' "$credentials" |
        curl --silent --show-error --fail --max-time 10 --cacert "$BAO_CACERT" \
            --header 'Content-Type: application/json' --request POST --data-binary @- \
            "$BAO_ADDR/v1/auth/standalone-master/login" 2>/dev/null |
        jq -er 'select(.auth.renewable == true and .auth.policies == ["ecpds-master"]) |
                .auth.client_token | select(type == "string" and length > 0)' > "$temporary"; then
        rm -f "$temporary"
        return 1
    fi
    if ! chown ecpds-master:ecpds-master "$temporary" ||
        ! chmod 0600 "$temporary" || ! mv -f "$temporary" "$token"; then
        rm -f "$temporary"
        return 1
    fi
}

for ((attempt = 0; attempt < 120; attempt++)); do
    if health | jq -e 'has("initialized") and has("sealed")' >/dev/null 2>&1; then
        break
    fi
    kill -0 "$server" 2>/dev/null || { echo "[openbao] Server exited." >&2; exit 1; }
    sleep 1
done
if [ "$mode" = manual ] || [ "$mode" = auto ]; then
    echo "[openbao] Waiting for initialization, an unsealed vault and Master token provisioning."
    while kill -0 "$server" 2>/dev/null; do
        if health | jq -e '.initialized and (.sealed | not)' >/dev/null 2>&1 &&
            renew_master_token; then
            touch "$ready"
            echo "[openbao] Embedded vault is ready."
            elapsed=0
            while kill -0 "$server" 2>/dev/null; do
                sleep 5 &
                wait $! || true
                if ! health | jq -e '.initialized and (.sealed | not)' >/dev/null 2>&1; then
                    rm -f "$ready"
                    echo "[openbao] Vault is sealed or unavailable." >&2
                    break
                fi
                elapsed=$((elapsed + 5))
                if [ "$elapsed" -ge 300 ]; then
                    if ! renew_master_token; then
                        rm -f "$ready"
                        echo "[openbao] Master token renewal failed; operator intervention may be required." >&2
                        break
                    fi
                    elapsed=0
                fi
            done
        fi
        sleep 5 &
        wait $! || true
    done
    echo "[openbao] Server stopped." >&2
    exit 1
fi
state=$(health)
if ! jq -e '.initialized' <<<"$state" >/dev/null; then
    if [ -e "$init" ] || [ -e "$init.pending" ]; then
        echo "[openbao] Vault state and recovery material do not match." >&2
        exit 1
    fi
    curl --silent --show-error --fail --max-time 120 --cacert "$BAO_CACERT" \
        --header 'Content-Type: application/json' \
        --data '{"secret_shares":1,"secret_threshold":1}' \
        "$BAO_ADDR/v1/sys/init" > "$init.pending"
    jq -e '.root_token and .keys_base64[0]' "$init.pending" >/dev/null
    mv "$init.pending" "$init"
fi
if [ ! -f "$init" ] && [ -f "$init.pending" ]; then
    jq -e '.root_token and .keys_base64[0]' "$init.pending" >/dev/null
    mv "$init.pending" "$init"
fi
if [ ! -f "$init" ]; then
    echo "[openbao] Recovery material is missing; restore the demo volume backup." >&2
    exit 1
fi
if jq -e '.sealed' <<<"$state" >/dev/null; then
    jq -c '{key: .keys_base64[0]}' "$init" |
        curl --silent --show-error --fail --max-time 30 --cacert "$BAO_CACERT" \
            --header 'Content-Type: application/json' --request PUT \
            --data-binary @- "$BAO_ADDR/v1/sys/unseal" >/dev/null
fi
health | jq -e '.initialized and (.sealed | not)' >/dev/null

export BAO_TOKEN
BAO_TOKEN=$(jq -er '.root_token' "$init")
if ! /usr/local/bin/bao secrets list -format=json | jq -e 'has("ecpds/")' >/dev/null; then
    /usr/local/bin/bao secrets enable -path=ecpds kv-v2 >/dev/null
fi
/usr/local/bin/bao policy write ecpds-master /etc/ecpds/openbao-policy.hcl >/dev/null

renew_or_create_token() {
    if [ -s "$token" ] && BAO_TOKEN="$(<"$token")" /usr/local/bin/bao token renew >/dev/null 2>&1; then
        return
    fi
    /usr/local/bin/bao token create -format=json -policy=ecpds-master -no-default-policy \
        -orphan -period=24h > "$operator/token.json"
    jq -er '.auth.client_token' "$operator/token.json" > "$operator/token.new"
    chown ecpds-master:ecpds-master "$operator/token.new"
    chmod 0600 "$operator/token.new"
    mv -f "$operator/token.new" "$token"
    rm -f "$operator/token.json"
}

renew_or_create_token
touch "$ready"
echo "[openbao] Embedded demo vault is ready (automatic unseal material is stored on /data)."
elapsed=0
while kill -0 "$server" 2>/dev/null; do
    sleep 5 &
    wait $! || true
    if ! kill -0 "$server" 2>/dev/null; then
        break
    fi
    elapsed=$((elapsed + 5))
    if [ "$elapsed" -ge 300 ]; then
        renew_or_create_token || echo "[openbao] Could not renew the Master token." >&2
        elapsed=0
    fi
done
echo "[openbao] Server stopped." >&2
exit 1
