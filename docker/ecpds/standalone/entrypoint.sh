#!/bin/bash
set -e

# ── Create all required runtime directories under /data ───────────────────────
mkdir -p /data/db \
         /data/log/{master,mover,monitor} \
         /data/tmp/{master,mover,monitor} \
         /data/lib/master \
         /data/lib/mover/mqtt \
         /data/lib/mover/data \
         /data/lib/monitor \
         /run/mysqld
chown mysql:mysql /data/db /run/mysqld

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
