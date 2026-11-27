#!/bin/bash
# Wait for Master callback port before starting the Monitor
echo "[monitor] Waiting for Master (localhost:9600)..."
until nc -z localhost 9600 2>/dev/null; do sleep 3; done
echo "[monitor] Master is ready."

export PORT_CALLBACK=8600
export PORT_HTTPS=8443
export PORT_MASTER=9600
export MASTER_ADDRESS=localhost
export LOG_LEVEL=debug
export JAVA_OPTS="${JAVA_OPTS:-} -Dpolyglot.engine.userResourceCache=/data/tmp/monitor/graal"
export JOLOKIA_PASSWORD
JOLOKIA_PASSWORD=${JOLOKIA_PASSWORD:-$(< /data/lib/monitor/standalone-secrets/jolokia)}
export KEYSTORE_PASSWORD
KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD:-$(< /data/lib/monitor/standalone-secrets/keystore)}
if [ "${ECPDS_INTERNAL_MTLS:-true}" = true ]; then
    export JAVA_OPTS="${JAVA_OPTS:-} -Decpds.rmi.tls.keyStore=/data/lib/monitor/internal-rmi/key.p12 -Decpds.rmi.tls.trustStore=/data/lib/monitor/internal-rmi/trust.p12 -Decpds.rmi.tls.passwordFile=/data/lib/monitor/internal-rmi/password"
fi

echo "[monitor] Starting..."
argfile=/data/tmp/monitor/monitor.ini
if [ -L "$argfile" ] || { [ -e "$argfile" ] && [ ! -f "$argfile" ]; }; then
    echo "[monitor] Invalid JVM argument file." >&2
    exit 1
fi
(umask 077; touch "$argfile") && chmod 0600 "$argfile" || exit 1
umask 007
exec /usr/local/ecpds/monitor/sh/monitor start
