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

# The transfer group has 4 volumes, all on /data. A volume that does not exist is seen as
# having no capacity and never receives data, so create them all up front.
for i in 0 1 2 3; do
    mkdir -p "/data/lib/mover/data/volume$i"
done

echo "[mover] Starting..."
exec /usr/local/ecpds/mover/sh/mover start
