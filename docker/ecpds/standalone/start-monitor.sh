#!/bin/bash
# Wait for Master callback port before starting the Monitor
echo "[monitor] Waiting for Master (localhost:9600)..."
until nc -z localhost 9600 2>/dev/null; do sleep 3; done
echo "[monitor] Master is ready."

export PORT_JMX=8062
export PORT_CALLBACK=8600
export PORT_HTTPS=8443
export PORT_PORT=8080
export PORT_MASTER=9600
export MASTER_ADDRESS=localhost

echo "[monitor] Starting..."
exec /usr/local/ecpds/monitor/sh/monitor start
