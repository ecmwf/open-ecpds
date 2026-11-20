#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."
bash openbao/standalone-openbao-make-cert.sh openbao/tls openbao/operator
umask 077
mkdir -p openbao/operator
exec docker compose -f compose.openbao.yml up -d openbao
