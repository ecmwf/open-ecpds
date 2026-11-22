#!/bin/bash
set -euo pipefail

if [ "$#" -ne 0 ] || [ ! -f /data/.standalone-build-id ] || [ -L /data/.standalone-build-id ]; then
    echo "[standalone] Renewal requires an existing standalone data directory and no arguments." >&2
    exit 1
fi
/usr/local/ecpds/standalone/check-data-version.sh /data /etc/ecpds/standalone-build-id /etc/ecpds/standalone-release
. /usr/local/ecpds/standalone/data-lock.sh

state=/data/lib/standalone
complete=$state/internal-rmi-complete
incomplete=$state/internal-rmi-incomplete
if [ ! -f "$complete" ] || [ -L "$complete" ] || [ -e "$incomplete" ] || [ -L "$incomplete" ]; then
    echo "[standalone] Internal RMI TLS is not initialized or a previous operation was interrupted." >&2
    exit 1
fi
for dir in "$state/internal-rmi-ca" /data/lib/{master,mover,monitor}/internal-rmi; do
    if [ ! -d "$dir" ] || [ -L "$dir" ]; then
        echo "[standalone] Existing internal RMI TLS material is missing or invalid: $dir" >&2
        exit 1
    fi
done

umask 077
work=$(mktemp -d "$state/internal-rmi-renewal.XXXXXXXX")
mkdir -p "$work/new" "$work/previous/lib/standalone"
for service in master mover monitor; do
    mkdir -p "$work/previous/lib/$service"
done
echo "[standalone] Generating replacement internal RMI identities."
/usr/local/ecpds/standalone/internal-rmi-tls.sh --generate "$work/new"
sync -f /data

echo "[standalone] Installing replacement identities; interrupted installation requires a data backup restore."
: > "$incomplete"
sync -f /data
mv "$state/internal-rmi-ca" "$work/previous/lib/standalone/internal-rmi-ca"
for service in master mover monitor; do
    mv "/data/lib/$service/internal-rmi" "$work/previous/lib/$service/internal-rmi"
done
mv "$work/new/lib/standalone/internal-rmi-ca" "$state/internal-rmi-ca"
for service in master mover monitor; do
    mv "$work/new/lib/$service/internal-rmi" "/data/lib/$service/internal-rmi"
done
sync -f /data
rm -f "$incomplete"
sync -f /data
echo "[standalone] Internal RMI identities replaced. Previous private material remains root-only in $work."
