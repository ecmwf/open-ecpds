#!/bin/bash
# Master uses the default callback port; set JMX port explicitly
export PORT_JMX=9062
export PORT_CALLBACK=9600
export PORT_ECPDS=9640
export PORT_FTP=

echo "[master] Starting..."
exec /usr/local/ecpds/master/sh/master start
