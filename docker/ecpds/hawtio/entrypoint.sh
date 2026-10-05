#!/bin/bash
set -euo pipefail

for envfile in default/hawtio.cnf hawtio.cnf; do
    if [[ -f "/etc/ecpds/$envfile" ]]; then
        source "/etc/ecpds/$envfile"
    fi
done
export HAWTIO_PRESET_CONNECTIONS

: "${HAWTIO_USER:?Set HAWTIO_USER for the gateway login}"
if [[ "$HAWTIO_USER" == *:* || "$HAWTIO_USER" == *$'\n'* || "$HAWTIO_USER" == *$'\r'* ]]; then
    echo "HAWTIO_USER must not contain a colon or a newline." >&2
    exit 1
fi
if [[ -n "${HAWTIO_PASSWORD_FILE:-}" ]]; then
    HAWTIO_PASSWORD=$(cat "$HAWTIO_PASSWORD_FILE")
fi
: "${HAWTIO_PASSWORD:?Set HAWTIO_PASSWORD or HAWTIO_PASSWORD_FILE}"
umask 077
mkdir -p /run/hawtio
password_hash=$(printf '%s\n' "$HAWTIO_PASSWORD" | openssl passwd -apr1 -stdin)
printf '%s:%s\n' "$HAWTIO_USER" "$password_hash" > /run/hawtio/htpasswd
unset HAWTIO_PASSWORD password_hash
chmod 0644 /run/hawtio/htpasswd
chmod 0755 /run/hawtio
cat > /run/hawtio/nginx.conf <<'EOF'
user nginx;
worker_processes auto;
pid /run/hawtio/nginx.pid;
error_log /dev/stderr;
events { worker_connections 1024; }
http {
    access_log /dev/stdout;
    server {
        listen 8080;
        location / {
            auth_basic "Hawtio";
            auth_basic_user_file /run/hawtio/htpasswd;
            proxy_pass http://127.0.0.1:8081;
            proxy_set_header Host $host;
        }
    }
}
EOF
nginx -t -c /run/hawtio/nginx.conf

java_pid=
nginx_pid=
cleanup() {
    trap '' TERM INT
    [[ -z "$nginx_pid" ]] || kill -TERM "$nginx_pid" 2>/dev/null || true
    [[ -z "$java_pid" ]] || kill -TERM "$java_pid" 2>/dev/null || true
    wait || true
}
trap cleanup EXIT
trap 'exit 0' TERM INT

# Never expose Jetty directly, even if a caller supplies HAWTIO_HOST/HAWTIO_PORT.
HAWTIO_HOST=127.0.0.1 HAWTIO_PORT=8081 \
    java ${JAVA_OPTS:-} -cp "/opt/hawtio/classes:/opt/hawtio/lib/*" HawtioLauncher &
java_pid=$!
nginx -c /run/hawtio/nginx.conf -g 'daemon off;' &
nginx_pid=$!
status=0
wait -n "$java_pid" "$nginx_pid" || status=$?
echo "Hawtio or nginx exited unexpectedly (status $status); stopping container." >&2
exit 1
