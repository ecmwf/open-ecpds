#!/bin/bash
# Wait for Master callback port before starting the Mover
echo "[mover] Waiting for Master (localhost:9600)..."
until nc -z localhost 9600 2>/dev/null; do sleep 3; done
echo "[mover] Master is ready."

export PORT_CALLBACK=7600
export PORT_HTTPS=7443
export PORT_MQTTS=8883
export PORT_ECPROXY=7640
export PORT_FTP=
export PORT_SSH=7022
export PORT_MASTER=9600
export MASTER_ADDRESS=localhost
# Must match TRS_NAME in the database; INTERNAL_ADDRESS stays localhost for RMI
export EXTERNAL_ADDRESS=ecpds-mover
export INTERNAL_ADDRESS=localhost
export LOG_LEVEL=debug
export JOLOKIA_PASSWORD
JOLOKIA_PASSWORD=${JOLOKIA_PASSWORD:-$(< /data/lib/mover/standalone-secrets/jolokia)}
export CLI_SHARED_SECRET RCC_SHARED_SECRET KEYSTORE_PASSWORD
CLI_SHARED_SECRET=${CLI_SHARED_SECRET:-$(< /data/lib/mover/standalone-secrets/cli)}
RCC_SHARED_SECRET=${RCC_SHARED_SECRET:-$(< /data/lib/mover/standalone-secrets/rcc)}
KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD:-$(< /data/lib/mover/standalone-secrets/keystore)}
if [ "${ECPDS_INTERNAL_MTLS:-true}" = true ]; then
    export JAVA_OPTS="${JAVA_OPTS:-} -Decpds.rmi.tls.keyStore=/data/lib/mover/internal-rmi/key.p12 -Decpds.rmi.tls.trustStore=/data/lib/mover/internal-rmi/trust.p12 -Decpds.rmi.tls.passwordFile=/data/lib/mover/internal-rmi/password"
fi

# The transfer group has 4 volumes, all on /data. A volume that does not exist is seen as
# having no capacity and never receives data, so create them all up front.
for i in 0 1 2 3; do
    mkdir -p "/data/lib/mover/data/volume$i"
done

echo "[mover] Starting..."
argfile=/data/tmp/mover/mover.ini
if [ -L "$argfile" ] || { [ -e "$argfile" ] && [ ! -f "$argfile" ]; }; then
    echo "[mover] Invalid JVM argument file." >&2
    exit 1
fi
(umask 077; touch "$argfile") && chmod 0600 "$argfile" || exit 1
exec /usr/local/ecpds/mover/sh/mover start
