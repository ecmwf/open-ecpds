#!/bin/bash
set -euo pipefail

mode=${ECPDS_OPENBAO_ENABLED:-embedded}
if [ "$(id -u)" -ne 0 ] || [ ! -t 0 ] || { [ "$mode" != manual ] && [ "$mode" != auto ]; }; then
    echo "Run this command as root in an interactive terminal on a manual- or auto-mode standalone container." >&2
    exit 1
fi

export BAO_ADDR=https://127.0.0.1:8200
export BAO_CACERT=/data/lib/openbao/tls/server.crt
unset BAO_TOKEN
bao=/usr/local/bin/bao
token=/data/lib/master/openbao/token
umask 077

case "${1:-}" in
    init)
        echo "Record the recovery shares separately and securely outside /data."
        echo "The initial root token is shown only once; it is needed for configure."
        if [ "$mode" = auto ]; then
            exec "$bao" operator init -recovery-shares=5 -recovery-threshold=3
        fi
        exec "$bao" operator init -key-shares=5 -key-threshold=3
        ;;
    unseal)
        echo "Enter distinct recovery shares until the vault is unsealed."
        until "$bao" status -format=json | jq -e '.initialized and (.sealed | not)' >/dev/null 2>&1; do
            "$bao" operator unseal
        done
        ;;
    auto-login)
        if [ "$mode" != auto ]; then
            echo "Automatic Master login is available only in auto-unseal mode." >&2
            exit 1
        fi
        read -r -s -p "OpenBao root token: " BAO_TOKEN
        printf '\n'
        if [ -z "$BAO_TOKEN" ]; then
            echo "Root token must not be empty." >&2
            exit 1
        fi
        export BAO_TOKEN
        auths=$("$bao" auth list -format=json)
        if jq -e 'has("standalone-master/")' <<<"$auths" >/dev/null; then
            echo "The standalone-master auth mount already exists; inspect it rather than replacing it." >&2
            exit 1
        fi
        "$bao" auth enable -path=standalone-master approle >/dev/null
        "$bao" write auth/standalone-master/role/ecpds-master \
            token_policies=ecpds-master token_no_default_policy=true token_period=24h \
            secret_id_num_uses=0 secret_id_ttl=0 >/dev/null
        role_id=$("$bao" read -field=role_id auth/standalone-master/role/ecpds-master/role-id)
        secret_id=$("$bao" write -field=secret_id -f auth/standalone-master/role/ecpds-master/secret-id)
        echo "Record this JSON once in your independent secret manager, not on /data:"
        printf '%s\n%s\n' "$role_id" "$secret_id" | jq -M -R -s 'split("\n") | {role_id: .[0], secret_id: .[1]}'
        unset BAO_TOKEN auths role_id secret_id
        ;;
    configure)
        read -r -s -p "OpenBao root token: " BAO_TOKEN
        printf '\n'
        if [ -z "$BAO_TOKEN" ]; then
            echo "Root token must not be empty." >&2
            exit 1
        fi
        export BAO_TOKEN
        if ! "$bao" secrets list -format=json | jq -e 'has("ecpds/")' >/dev/null; then
            "$bao" secrets enable -path=ecpds kv-v2 >/dev/null
        fi
        "$bao" policy write ecpds-master /etc/ecpds/openbao-policy.hcl >/dev/null
        "$bao" policy write ecpds-recovery - >/dev/null <<'EOF'
path "sys/generate-root-token/attempt" {
  capabilities = ["read", "create", "update", "delete", "sudo"]
}
path "sys/generate-root-token/update" {
  capabilities = ["update", "sudo"]
}
path "auth/token/revoke-self" {
  capabilities = ["update"]
}
EOF
        auths=$("$bao" auth list -format=json)
        if jq -e 'has("standalone-recovery/")' <<<"$auths" >/dev/null; then
            if ! jq -e '.["standalone-recovery/"].type == "userpass"' <<<"$auths" >/dev/null ||
                ! "$bao" read -format=json auth/standalone-recovery/users/operator >/dev/null; then
                echo "Recovery login is incomplete or invalid; repair it using the root token before continuing." >&2
                exit 1
            fi
        else
            recovery_password=$(openssl rand -hex 32)
            echo "Record this recovery password securely outside /data, separately from the unseal shares:"
            printf 'Recovery password: %s\n' "$recovery_password"
            read -r -s -p "Re-enter recovery password to confirm: " confirmation
            printf '\n'
            if [ "$confirmation" != "$recovery_password" ]; then
                echo "Recovery password confirmation failed; root token was not revoked." >&2
                exit 1
            fi
            unset confirmation
            "$bao" auth enable -path=standalone-recovery userpass >/dev/null
            printf '%s' "$recovery_password" | "$bao" write auth/standalone-recovery/users/operator \
                password=- token_policies=ecpds-recovery token_no_default_policy=true \
                token_ttl=15m token_max_ttl=15m >/dev/null
            unset recovery_password
        fi
        unset auths
        if ! "$bao" read -format=json auth/standalone-recovery/users/operator |
            jq -e '.data.token_policies == ["ecpds-recovery"] and
                   .data.token_no_default_policy == true and
                   .data.token_ttl > 0 and .data.token_ttl <= 900 and
                   .data.token_max_ttl > 0 and .data.token_max_ttl <= 900' >/dev/null; then
            echo "Recovery login has unexpected permissions or token lifetime; root token was not revoked." >&2
            exit 1
        fi
        temporary=$(mktemp /run/ecpds/openbao-token.XXXXXXXX)
        trap 'rm -f "$temporary" "$temporary.json"' EXIT
        "$bao" token create -format=json -policy=ecpds-master -no-default-policy \
            -orphan -period=24h > "$temporary.json"
        jq -er '.auth.client_token' "$temporary.json" > "$temporary"
        chown ecpds-master:ecpds-master "$temporary"
        chmod 0600 "$temporary"
        mv -f "$temporary" "$token"
        "$bao" token revoke -self >/dev/null
        unset BAO_TOKEN
        echo "Master token provisioned and initial root token revoked."
        ;;
    recover)
        read -r -s -p "OpenBao recovery password: " recovery_password
        printf '\n'
        BAO_TOKEN=$(printf '%s' "$recovery_password" |
            "$bao" write -field=token auth/standalone-recovery/login/operator password=-)
        unset recovery_password
        export BAO_TOKEN
        recovery_in_progress=false
        cleanup_recovery() {
            if [ "$recovery_in_progress" = true ]; then
                "$bao" operator generate-root -cancel >/dev/null 2>&1 || true
            fi
            "$bao" token revoke -self >/dev/null 2>&1 || true
        }
        trap cleanup_recovery EXIT
        trap 'exit 1' INT TERM
        attempt=$("$bao" operator generate-root -init -format=json)
        recovery_in_progress=true
        nonce=$(jq -er '.nonce' <<<"$attempt")
        otp=$(jq -er '.otp' <<<"$attempt")
        complete=false
        echo "Enter distinct recovery shares until a root token is generated."
        while [ "$complete" != true ]; do
            read -r -s -p "Unseal share: " share
            printf '\n'
            result=$(printf '%s\n' "$share" |
                "$bao" operator generate-root -nonce="$nonce" -format=json -)
            unset share
            complete=$(jq -r '.complete' <<<"$result")
            if [ "$complete" != true ] && [ "$complete" != false ]; then
                echo "Invalid root generation response." >&2
                exit 1
            fi
        done
        recovery_in_progress=false
        encoded=$(jq -er '.encoded_token' <<<"$result")
        root_token=$(printf '%s' "$encoded" | "$bao" operator generate-root -decode=- -otp="$otp")
        echo "This temporary root token is shown only once; use it for configure:"
        printf 'Root token: %s\n' "$root_token"
        unset attempt nonce otp result encoded root_token
        ;;
    *)
        echo "Usage: $0 {init|unseal|auto-login|configure|recover}" >&2
        exit 1
        ;;
esac
