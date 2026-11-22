#!/bin/bash
set -euo pipefail

for service in database master mover monitor hawtio; do
    supervisorctl status "$service" | grep -Eq "^${service}[[:space:]]+RUNNING([[:space:]]|$)"
done
if [ "${ECPDS_OPENBAO_ENABLED:-embedded}" = manual ] || [ "${ECPDS_OPENBAO_ENABLED:-embedded}" = embedded ] || [ "${ECPDS_OPENBAO_ENABLED:-embedded}" = auto ]; then
    supervisorctl status openbao | grep -Eq '^openbao[[:space:]]+RUNNING([[:space:]]|$)'
    test -f /run/ecpds/openbao/ready
fi
timeout 5s mysql --defaults-extra-file=/data/lib/standalone/mysql-client.cnf \
    -N -e 'SELECT 1' ecpds >/dev/null
for port in 9600 7600 7443 8600 8443 8080; do
    nc -z -w 1 127.0.0.1 "$port"
done
