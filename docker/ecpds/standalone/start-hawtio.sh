#!/bin/bash
# Serve the Hawtio gateway over HTTPS with the Monitor certificate. The Monitor
# generates its keystore on first start, so wait for it before starting.
# Set HAWTIO_TLS_KEYSTORE to another PKCS#12 file to override, or to "none" for plain HTTP.
export HAWTIO_PASSWORD
HAWTIO_PASSWORD=${HAWTIO_PASSWORD:-$(< /data/lib/monitor/standalone-secrets/hawtio)}
export KEYSTORE_PASSWORD
KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD:-$(< /data/lib/monitor/standalone-secrets/keystore)}
keystore=${HAWTIO_TLS_KEYSTORE:-/data/lib/monitor/tls/ecpds-monitor.pfx}

if [[ "$keystore" == "none" ]]; then
    unset HAWTIO_TLS_KEYSTORE
else
    echo "[hawtio] Waiting for keystore $keystore..."
    until [[ -s "$keystore" ]]; do sleep 3; done
    # Give the Monitor time to finish writing/migrating the file.
    sleep 2
    export HAWTIO_TLS_KEYSTORE=$keystore
fi

echo "[hawtio] Starting..."
exec /entrypoint.sh
