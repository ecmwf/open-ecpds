#!/bin/bash
# Master uses the default callback port
export PORT_CALLBACK=9600
export PORT_ECPDS=9640
export LOG_LEVEL=debug
export JOLOKIA_PASSWORD
JOLOKIA_PASSWORD=${JOLOKIA_PASSWORD:-$(< /data/lib/master/standalone-secrets/jolokia)}
export CLI_SHARED_SECRET
CLI_SHARED_SECRET=${CLI_SHARED_SECRET:-$(< /data/lib/master/standalone-secrets/cli)}
if [ "${ECPDS_INTERNAL_MTLS:-true}" = true ]; then
    export JAVA_OPTS="${JAVA_OPTS:-} -Decpds.rmi.tls.keyStore=/data/lib/master/internal-rmi/key.p12 -Decpds.rmi.tls.trustStore=/data/lib/master/internal-rmi/trust.p12 -Decpds.rmi.tls.passwordFile=/data/lib/master/internal-rmi/password"
fi

echo "[master] Waiting for MariaDB..."
export MYSQL_PWD
MYSQL_PWD=$(< /data/lib/master/standalone-secrets/database)
until mysql --protocol=tcp -h 127.0.0.1 -u master -N -e 'SELECT 1' ecpds >/dev/null 2>&1; do
    sleep 3
done
unset MYSQL_PWD

if [ "${ECPDS_OPENBAO_ENABLED:-embedded}" = embedded ] || [ "${ECPDS_OPENBAO_ENABLED:-embedded}" = manual ] || [ "${ECPDS_OPENBAO_ENABLED:-embedded}" = auto ]; then
    echo "[master] Waiting for the embedded vault..."
    for ((attempt = 0; attempt < 120; attempt++)); do
        [ -f /run/ecpds/openbao/ready ] && break
        sleep 1
    done
    if [ ! -f /run/ecpds/openbao/ready ]; then
        echo "[master] Embedded vault is not ready." >&2
        exit 1
    fi
fi

echo "[master] Starting..."
argfile=/data/tmp/master/master.ini
if [ -L "$argfile" ] || { [ -e "$argfile" ] && [ ! -f "$argfile" ]; }; then
    echo "[master] Invalid JVM argument file." >&2
    exit 1
fi
(umask 077; touch "$argfile") && chmod 0600 "$argfile" || exit 1
exec /usr/local/ecpds/master/sh/master start
