#!/bin/bash
set -euo pipefail

generate_material() {
    local root=$1 state=$1/lib/standalone ca=$1/lib/standalone/internal-rmi-ca
    local incomplete=$1/lib/standalone/internal-rmi-incomplete complete=$1/lib/standalone/internal-rmi-complete
    local service dir
    install -d -o root -g root -m 0700 "$state"
    if [ -e "$incomplete" ] || [ -e "$complete" ] ||
        [ -e "$ca" ] || [ -e "$root/lib/master/internal-rmi" ] ||
        [ -e "$root/lib/mover/internal-rmi" ] || [ -e "$root/lib/monitor/internal-rmi" ]; then
        echo "[standalone] Incomplete internal RMI TLS material exists; refusing to overwrite it." >&2
        exit 1
    fi
    umask 077
    : > "$incomplete"
    install -d -m 0700 "$ca"
    openssl req -x509 -newkey rsa:3072 -nodes -days 3650 \
        -keyout "$ca/ca.key" -out "$ca/ca.crt" -subj '/CN=OpenECPDS standalone internal CA' \
        -addext 'basicConstraints=critical,CA:TRUE' -addext 'keyUsage=critical,keyCertSign,cRLSign' >/dev/null 2>&1
    printf '%s\n' 'basicConstraints=critical,CA:FALSE' \
        'keyUsage=critical,digitalSignature,keyEncipherment' \
        'extendedKeyUsage=serverAuth,clientAuth' \
        'subjectAltName=DNS:localhost,DNS:ecpds-mover,IP:127.0.0.1,IP:::1' > "$ca/leaf.ext"
    for service in master mover monitor; do
        dir=$root/lib/$service/internal-rmi
        install -d -m 0700 "$dir"
        openssl req -new -newkey rsa:3072 -nodes -keyout "$dir/key.pem" \
            -out "$dir/request.pem" -subj "/CN=standalone-$service" >/dev/null 2>&1
        openssl x509 -req -in "$dir/request.pem" -CA "$ca/ca.crt" -CAkey "$ca/ca.key" \
            -CAcreateserial -days 3650 -extfile "$ca/leaf.ext" -out "$dir/cert.pem" >/dev/null 2>&1
        export ECPDS_RMI_PASSWORD
        ECPDS_RMI_PASSWORD=$(openssl rand -hex 24)
        printf '%s\n' "$ECPDS_RMI_PASSWORD" > "$dir/password"
        openssl pkcs12 -export -inkey "$dir/key.pem" -in "$dir/cert.pem" \
            -name "$service" -out "$dir/key.p12" -passout env:ECPDS_RMI_PASSWORD >/dev/null 2>&1
        keytool -importcert -noprompt -storetype PKCS12 -keystore "$dir/trust.p12" \
            -storepass:env ECPDS_RMI_PASSWORD -alias standalone-rmi-ca -file "$ca/ca.crt" >/dev/null 2>&1
        unset ECPDS_RMI_PASSWORD
        rm -f "$dir/key.pem" "$dir/request.pem" "$dir/cert.pem"
        chown -R "ecpds-$service:ecpds-$service" "$dir"
        chmod 0600 "$dir/key.p12" "$dir/trust.p12" "$dir/password"
    done
    rm -f "$ca/leaf.ext"
    : > "$complete"
    rm -f "$incomplete"
}

if [ "${1:-}" = --generate ]; then
    [ "$#" -eq 2 ] && [ -d "$2" ] || exit 1
    generate_material "$2"
    exit 0
fi
[ "$#" -eq 0 ] || exit 1

mode=${ECPDS_INTERNAL_MTLS:-true}
if [ "$mode" = false ]; then
    if [ -e /data/lib/standalone/internal-rmi-complete ] ||
        [ -e /data/lib/standalone/internal-rmi-incomplete ]; then
        echo "[standalone] Refusing to disable previously initialized internal RMI mutual TLS." >&2
        exit 1
    fi
    exit 0
fi
if [ "$mode" != true ]; then
    echo "[standalone] ECPDS_INTERNAL_MTLS must be true or false." >&2
    exit 1
fi

state=/data/lib/standalone
incomplete=$state/internal-rmi-incomplete
complete=$state/internal-rmi-complete
install -d -o root -g root -m 0700 "$state"
if [ -e "$incomplete" ]; then
    echo "[standalone] Internal RMI TLS initialization was interrupted; restore the data directory or repair it." >&2
    exit 1
fi
if [ ! -e "$complete" ]; then
    generate_material /data
fi

for service in master mover monitor; do
    dir=/data/lib/$service/internal-rmi
    for file in key.p12 trust.p12 password; do
        if [ ! -f "$dir/$file" ] || [ -L "$dir/$file" ] ||
            ! runuser -u "ecpds-$service" -- test -r "$dir/$file"; then
            echo "[standalone] Internal RMI TLS material for $service is missing or unreadable." >&2
            exit 1
        fi
    done
    config=/usr/local/ecpds/$service/conf/ecmwf.properties
    awk '
        /^\[CallBack\]$/ {
            callback = 1; found = 1; print
            print "requireMutualTls=true"
            print "serverFactory=ecmwf.common.rmi.StrictRmiTlsSocketFactory"
            print "clientFactory=ecmwf.common.rmi.StrictRmiTlsSocketFactory"
            next
        }
        /^\[/ && callback { callback = 0 }
        callback && /^(requireMutualTls|serverFactory|clientFactory)=/ { next }
        { print }
        END { if (!found) exit 1 }
    ' "$config" > "$config.standalone" || {
        rm -f "$config.standalone"
        echo "[standalone] Missing callback configuration for $service." >&2
        exit 1
    }
    cat "$config.standalone" > "$config"
    rm -f "$config.standalone"
done
