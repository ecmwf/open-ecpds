#!/bin/bash
set -e

# ── Resolve internal hostnames ─────────────────────────────────────────────────
echo "127.0.0.1 ecpds-mover" >> /etc/hosts

# ── Create all required runtime directories under /data ───────────────────────
mkdir -p /data/db \
         /data/log/{master,mover,monitor} \
         /data/tmp/{master,mover,monitor} \
         /data/lib/master \
         /data/lib/mover/mqtt \
         /data/lib/mover/data \
         /data/lib/monitor \
         /etc/ecpds/{master,mover,monitor} \
         /run/mysqld /run/hawtio /var/lib/nginx/tmp
chown mysql:mysql /data/db /run/mysqld
chown ecpds-monitor:ecpds-monitor /run/hawtio /var/lib/nginx/tmp
chmod 0755 /var/lib/nginx
touch /var/log/nginx/error.log
chown ecpds-monitor:ecpds-monitor /var/log/nginx/error.log

# Services used to run as root. On the first upgraded start, transfer their
# existing files once, then keep ownership current for newly-created directories.
if [ ! -e /data/.standalone-service-users-v1 ]; then
    echo "[standalone] Migrating persisted service data ownership; first upgraded startup may take a while..."
    for service in master mover monitor; do
        chown -R "ecpds-${service}:ecpds-${service}" \
            "/data/log/${service}" "/data/tmp/${service}" "/data/lib/${service}"
    done
    touch /data/.standalone-service-users-v1
    chmod 0600 /data/.standalone-service-users-v1
    echo "[standalone] Service data ownership migration complete."
fi
chown ecpds-master:ecpds-master /data/log/master /data/tmp/master /data/lib/master
chown ecpds-mover:ecpds-mover /data/log/mover /data/tmp/mover /data/lib/mover
chown ecpds-monitor:ecpds-monitor /data/log/monitor /data/tmp/monitor /data/lib/monitor
chown ecpds-mover:ecpds-mover /data/lib/mover/data /data/lib/mover/mqtt
for service in master mover monitor; do
    if [ -d "/etc/ecpds/${service}" ]; then
        chown "ecpds-${service}:ecpds-${service}" "/etc/ecpds/${service}"
    fi
done
if [ -f /etc/ecpds/monitor/ecpds-monitor.pfx ]; then
    chown ecpds-monitor:ecpds-monitor /etc/ecpds/monitor/ecpds-monitor.pfx
    chmod 0640 /etc/ecpds/monitor/ecpds-monitor.pfx
fi

# OpenBao is opt-in. The source token must be a protected operator-managed file;
# it is copied into /run (normally tmpfs) with Master-only ownership.
if [ "${ECPDS_OPENBAO_ENABLED:-false}" = true ]; then
    token_source=/run/ecpds/openbao/token-source
    ca_source=/etc/pki/ca-trust/source/anchors/openbao.crt
    chown ecpds-master:ecpds-master /run/ecpds/openbao
    chmod 0700 /run/ecpds/openbao
    if [ ! -f "$token_source" ] || [ ! -f "$ca_source" ]; then
        echo "[standalone] OpenBao enabled but token source or CA certificate is missing." >&2
        exit 1
    fi
    token_mode=$(stat -c '%a' "$token_source")
    if (( (8#$token_mode & 077) != 0 )); then
        echo "[standalone] OpenBao token source must have no group/other permissions." >&2
        exit 1
    fi
    install -o ecpds-master -g ecpds-master -m 0600 "$token_source" /run/ecpds/openbao/token
    update-ca-trust extract
    keytool -importcert -noprompt -alias standalone-openbao-ca \
        -file "$ca_source" -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit >/dev/null 2>&1 || {
        keytool -delete -alias standalone-openbao-ca \
            -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit >/dev/null 2>&1
        keytool -importcert -noprompt -alias standalone-openbao-ca \
            -file "$ca_source" -keystore "$JAVA_HOME/lib/security/cacerts" \
            -storepass changeit >/dev/null 2>&1
    }
    master_config=/usr/local/ecpds/master/conf/ecmwf.properties
    awk '
        /^\[OpenBao\]$/ {
            in_openbao = 1
            found = 1
            print
            print "enabled=yes"
            print "address=https://openbao:8200"
            print "mount=ecpds"
            print "tokenFile=/run/ecpds/openbao/token"
            next
        }
        /^\[/ && in_openbao { in_openbao = 0 }
        in_openbao && /^(enabled|address|mount|tokenFile)=/ { next }
        { print }
        END { if (!found) exit 1 }
    ' "$master_config" > "${master_config}.standalone" || {
        rm -f "${master_config}.standalone"
        echo "[standalone] Master OpenBao configuration section is missing." >&2
        exit 1
    }
    cat "${master_config}.standalone" > "$master_config"
    rm -f "${master_config}.standalone"
    echo "[standalone] Master-only OpenBao token installed; local-password mode remains in use."
elif [ "${ECPDS_OPENBAO_ENABLED:-false}" != false ]; then
    echo "[standalone] ECPDS_OPENBAO_ENABLED must be true or false." >&2
    exit 1
fi

# ── Initialise MariaDB on first run ────────────────────────────────────────────
if [ ! -d /data/db/ecpds ]; then
    echo "[standalone] Initialising MariaDB database..."
    mysql_install_db --user=mysql --datadir=/data/db --skip-name-resolve \
        >/dev/null 2>&1 || true

    # Start MariaDB temporarily (no root password yet)
    mysqld_safe --user=mysql &
    INIT_PID=$!

    echo -n "[standalone] Waiting for MariaDB"
    until mysqladmin ping --socket=/run/mysqld/mysqld.sock --silent 2>/dev/null; do
        echo -n "."
        sleep 1
    done
    echo " ready."

    # Create database, user, and schema
    mysql --socket=/run/mysqld/mysqld.sock -u root < /ecpds-init.sql
    echo "[standalone] Schema loaded."

    # Remove anonymous users that would block master@% access
    mysql --socket=/run/mysqld/mysqld.sock -u root -e \
        "DELETE FROM mysql.user WHERE user=''; FLUSH PRIVILEGES;"

    # Set root password
    mysqladmin --socket=/run/mysqld/mysqld.sock -u root password "${MYSQL_ROOT_PASSWORD}"

    mysqladmin --socket=/run/mysqld/mysqld.sock \
               -u root -p"${MYSQL_ROOT_PASSWORD}" shutdown 2>/dev/null || true
    wait $INIT_PID 2>/dev/null || true
    sleep 2
    echo "[standalone] Database initialised."
fi

# ── Start MariaDB for normal operation ────────────────────────────────────────
echo "[standalone] Starting MariaDB..."
mysqld_safe --user=mysql &

echo -n "[standalone] Waiting for MariaDB"
until mysqladmin ping --socket=/run/mysqld/mysqld.sock \
      -u root -p"${MYSQL_ROOT_PASSWORD}" --silent 2>/dev/null; do
    echo -n "."
    sleep 1
done
echo " ready."

# ── Hand off to supervisord (becomes PID 1 replacement) ────────────────────────
echo "[standalone] Starting all OpenECPDS services..."
exec /usr/bin/supervisord -c /etc/supervisord.conf
