#!/bin/bash
set -euo pipefail

for envfile in default/hawtio.cnf hawtio.cnf; do
    if [[ -f "/etc/ecpds/$envfile" ]]; then
        source "/etc/ecpds/$envfile"
    fi
done
export HAWTIO_PRESET_CONNECTIONS

gateway_port=${HAWTIO_GATEWAY_PORT:-8080}
if [[ ! "$gateway_port" =~ ^[0-9]{1,5}$ ]] ||
    (( 10#$gateway_port < 1 || 10#$gateway_port > 65535 || 10#$gateway_port == 8081 )); then
    echo "HAWTIO_GATEWAY_PORT must be between 1 and 65535, excluding private Jetty port 8081." >&2
    exit 1
fi
gateway_port=$((10#$gateway_port))

access_log=${HAWTIO_ACCESS_LOG:-/dev/stdout}
if [[ "$access_log" != "off" && ! "$access_log" =~ ^/[A-Za-z0-9._/-]+$ ]]; then
    echo "HAWTIO_ACCESS_LOG must be 'off' or an absolute file path." >&2
    exit 1
fi

nginx_workers=${HAWTIO_NGINX_WORKER_PROCESSES:-2}
if [[ ! "$nginx_workers" =~ ^[1-9][0-9]*$ ]]; then
    echo "HAWTIO_NGINX_WORKER_PROCESSES must be a positive integer." >&2
    exit 1
fi

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
html_escape() {
    local value=$1
    value=${value//&/&amp;}
    value=${value//</&lt;}
    value=${value//>/&gt;}
    value=${value//\"/&quot;}
    value=${value//\'/&#39;}
    printf '%s' "$value"
}
cat > /run/hawtio/index.html <<'EOF'
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>ECPDS Hawtio</title>
<style>
:root{color-scheme:light;--accent:#0079d3;--border:#dee2e6;--muted:#59636e}
*{box-sizing:border-box}
body{margin:0;background:#f5f7fa;color:#212529;font:15px/1.5 system-ui,-apple-system,"Segoe UI",sans-serif}
header{background:#fff;border-bottom:1px solid var(--border);border-top:4px solid var(--accent)}
.header-inner,main{max-width:1080px;margin:auto;padding:24px}
.brand{font-size:12px;font-weight:700;letter-spacing:.12em;color:var(--accent)}
h1{margin:5px 0 0;font-size:27px;font-weight:600}
.subtitle{margin:5px 0 0;color:var(--muted)}
main{padding-top:32px}
.info{padding:15px 18px;border-left:4px solid var(--accent);border-radius:5px;background:#eaf3fb;margin-bottom:26px}
h2{font-size:18px;margin:0 0 16px;font-weight:600}
.connections{list-style:none;padding:0;margin:0;display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,260px),1fr));gap:16px}
.connection{display:flex;align-items:center;gap:14px;padding:22px;background:#fff;border:1px solid var(--border);border-radius:8px;text-decoration:none;color:inherit;box-shadow:0 2px 5px rgba(0,0,0,.03);height:100%;transition:border-color .15s,box-shadow .15s}
.connection:hover{border-color:var(--accent);box-shadow:0 3px 12px rgba(0,121,211,.12)}
.connection:focus-visible{outline:3px solid var(--accent);outline-offset:3px}
.icon{display:grid;place-items:center;width:44px;height:44px;flex-shrink:0;border-radius:9px;background:#eaf3fb;color:var(--accent)}
.icon svg{width:24px;height:24px}
.name{display:block;font-size:17px;font-weight:600;overflow-wrap:anywhere}
.description{display:block;font-size:13px;color:var(--muted);margin-top:3px}
.arrow{margin-left:auto;color:var(--accent);font-size:21px}
.empty{padding:24px;background:#fff;border:1px dashed var(--border);border-radius:8px;color:var(--muted)}
footer{margin-top:26px;color:var(--muted);font-size:13px}
@media(prefers-reduced-motion:reduce){.connection{transition:none}}
</style>
</head>
<body>
<header><div class="header-inner">
<div class="brand">ECPDS / ADMINISTRATION</div>
<h1>ECPDS Hawtio</h1>
<p class="subtitle">Java service monitoring and management</p>
</div></header>
<main>
<div class="info">Select a configured service to open its Hawtio console in a new tab. Sign in with the service's Jolokia credentials when prompted.</div>
<h2>Configured connections</h2>
<ul class="connections">
EOF
connection_count=0
IFS=',' read -r -a preset_connections <<< "${HAWTIO_PRESET_CONNECTIONS:-}"
for connection in "${preset_connections[@]}"; do
    connection=${connection#"${connection%%[![:space:]]*}"}
    connection=${connection%"${connection##*[![:space:]]}"}
    [[ "$connection" == *=* ]] || continue
    connection_id=${connection%%=*}
    connection_url=${connection#*=}
    if [[ ! "$connection_id" =~ ^[a-zA-Z0-9._-]+$ || -z "$connection_url" ]]; then
        echo "Skipping invalid Hawtio preset connection: $connection_id" >&2
        continue
    fi
    printf '<li><a class="connection" href="/hawtio/?con=%s" target="_blank" rel="noopener noreferrer"><span class="icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="3" width="18" height="7" rx="2"/><rect x="3" y="14" width="18" height="7" rx="2"/><path d="M7 6.5h2M7 17.5h2M14 6.5h3M14 17.5h3"/></svg></span><span><span class="name">%s</span><span class="description">Open console in a new tab</span></span><span class="arrow" aria-hidden="true">&#8599;</span></a></li>\n' \
        "$connection_id" "$(html_escape "$connection_id")" >> /run/hawtio/index.html
    connection_count=$((connection_count + 1))
done
if (( connection_count == 0 )); then
    printf '<li class="empty">No connections are configured. Ask your administrator to configure the service presets.</li>\n' >> /run/hawtio/index.html
fi
cat >> /run/hawtio/index.html <<'EOF'
</ul>
<footer>Only configured connections are listed. Management operations in the console may affect running services.</footer>
</main>
</body>
</html>
EOF
chmod 0644 /run/hawtio/index.html
cat > /run/hawtio/nginx.conf <<'EOF'
user nginx;
worker_processes $nginx_workers;
pid /run/hawtio/nginx.pid;
error_log /dev/stderr;
events { worker_connections 1024; }
http {
    access_log /dev/stdout;
    # Jetty only sees plain HTTP from nginx and rejects a browser "https://" Origin.
    map $http_origin $hawtio_origin {
        default $http_origin;
        "~^https://(?<origin_rest>.+)$" "http://$origin_rest";
    }
    server {
        listen 8080;
        location = / {
            auth_basic "Hawtio";
            auth_basic_user_file /run/hawtio/htpasswd;
            root /run/hawtio;
            try_files /index.html =404;
        }
        location / {
            auth_basic "Hawtio";
            auth_basic_user_file /run/hawtio/htpasswd;
            proxy_pass http://127.0.0.1:8081;
            proxy_set_header Host $host;
            proxy_set_header Origin $hawtio_origin;
        }
        # A Jolokia 401 relayed by the Hawtio proxy makes browsers discard the gateway's
        # cached Basic credentials and prompt again, so report it as 403 instead.
        location /hawtio/proxy/ {
            auth_basic "Hawtio";
            auth_basic_user_file /run/hawtio/htpasswd;
            proxy_pass http://127.0.0.1:8081;
            proxy_set_header Host $host;
            proxy_set_header Origin $hawtio_origin;
            proxy_intercept_errors on;
            error_page 401 =403 @jolokia_denied;
        }
        location @jolokia_denied {
            return 403;
        }
    }
}
EOF
sed -i -e "s#access_log /dev/stdout;#access_log $access_log;#" \
    -e "s/listen 8080;/listen $gateway_port;/" \
    -e 's/worker_processes \$nginx_workers;/worker_processes '"$nginx_workers"';/' /run/hawtio/nginx.conf
if [[ -n "${HAWTIO_TLS_KEYSTORE:-}" ]]; then
    if [[ -n "${KEYSTORE_PASSWORD_FILE:-}" ]]; then
        KEYSTORE_PASSWORD=$(cat "$KEYSTORE_PASSWORD_FILE")
    fi
    : "${KEYSTORE_PASSWORD:?Set KEYSTORE_PASSWORD or KEYSTORE_PASSWORD_FILE for HTTPS}"
    export KEYSTORE_PASSWORD
    openssl pkcs12 -in "$HAWTIO_TLS_KEYSTORE" -passin env:KEYSTORE_PASSWORD \
        -clcerts -nokeys -out /run/hawtio/certificate.pem
    openssl pkcs12 -in "$HAWTIO_TLS_KEYSTORE" -passin env:KEYSTORE_PASSWORD \
        -cacerts -nokeys -out /run/hawtio/chain.pem
    cat /run/hawtio/chain.pem >> /run/hawtio/certificate.pem
    openssl pkcs12 -in "$HAWTIO_TLS_KEYSTORE" -passin env:KEYSTORE_PASSWORD \
        -nocerts -nodes -out /run/hawtio/key.pem
    unset KEYSTORE_PASSWORD
    sed -i "s/listen $gateway_port;/listen $gateway_port ssl;/" /run/hawtio/nginx.conf
    sed -i '/        location \/ {/i\        ssl_certificate /run/hawtio/certificate.pem;\n        ssl_certificate_key /run/hawtio/key.pem;\n        ssl_protocols TLSv1.2 TLSv1.3;' /run/hawtio/nginx.conf
fi
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
