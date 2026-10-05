#!/bin/bash
# Master uses the default callback port
export PORT_CALLBACK=9600
export PORT_ECPDS=9640
export LOG_LEVEL=debug

echo "[master] Starting..."
exec /usr/local/ecpds/master/sh/master start
