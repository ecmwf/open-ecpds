#!/bin/bash
set -e

/usr/local/ecpds/standalone/check-data-version.sh /data /etc/ecpds/standalone-build-id /etc/ecpds/standalone-release

. /usr/local/ecpds/standalone/data-lock.sh

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
         /data/lib/mover/tls /data/lib/monitor/tls \
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
chown ecpds-mover:ecpds-mover /data/lib/mover/tls
chown ecpds-monitor:ecpds-monitor /data/lib/monitor/tls
for directory in /data/lib/mover/ssh /data/lib/mover/ssh/keys; do
    if [ -L "$directory" ] || { [ -e "$directory" ] && [ ! -d "$directory" ]; }; then
        echo "[standalone] SFTP host key directory must be a directory, not a symlink." >&2
        exit 1
    fi
    install -d -o ecpds-mover -g ecpds-mover -m 0700 "$directory"
done
for service in master mover monitor; do
    if [ -d "/etc/ecpds/${service}" ]; then
        chown "ecpds-${service}:ecpds-${service}" "/etc/ecpds/${service}"
    fi
done
if [ -f /etc/ecpds/monitor/ecpds-monitor.pfx ]; then
    chown ecpds-monitor:ecpds-monitor /etc/ecpds/monitor/ecpds-monitor.pfx
    chmod 0640 /etc/ecpds/monitor/ecpds-monitor.pfx
fi

openbao_mode=${ECPDS_OPENBAO_ENABLED:-embedded}
if [ "$openbao_mode" = embedded ] || [ "$openbao_mode" = manual ] || [ "$openbao_mode" = auto ]; then
    install -d -o ecpds-openbao -g ecpds-openbao -m 0755 /data/lib/openbao
    install -d -o ecpds-openbao -g ecpds-openbao -m 0700 /data/lib/openbao/raft
    install -d -o ecpds-openbao -g ecpds-openbao -m 0755 /data/lib/openbao/tls
    install -d -o root -g root -m 0700 /data/lib/standalone
    seal_marker=/data/lib/standalone/openbao-static-seal-key-id
    if [ "$openbao_mode" = auto ]; then
        seal_key=/run/secrets/ecpds-openbao-seal.key
        if [ -L "$seal_key" ] || [ ! -f "$seal_key" ] ||
            [ "$(stat -c '%u' "$seal_key")" != 12005 ] ||
            [ "$(stat -c '%s' "$seal_key")" != 32 ]; then
            echo "[standalone] Auto-unseal requires a 32-byte seal key owned by UID 12005 at $seal_key." >&2
            exit 1
        fi
        seal_mode=$(stat -c '%a' "$seal_key")
        if [ "$seal_mode" != 400 ]; then
            echo "[standalone] The auto-unseal key must have mode 0400." >&2
            exit 1
        fi
        seal_id=$(sha256sum "$seal_key")
        seal_id=${seal_id%% *}
        if [ -e "$seal_marker" ]; then
            if [ ! -f "$seal_marker" ] || [ "$(cat "$seal_marker")" != "$seal_id" ]; then
                echo "[standalone] The auto-unseal key does not match this vault." >&2
                exit 1
            fi
        else
            if [ -n "$(find /data/lib/openbao/raft -mindepth 1 -print -quit)" ]; then
                echo "[standalone] Existing vaults cannot be switched to auto-unseal automatically." >&2
                exit 1
            fi
            (umask 077; printf '%s\n' "$seal_id" > "$seal_marker")
        fi
        install -d -o ecpds-openbao -g ecpds-openbao -m 0700 /run/ecpds/openbao-seal
        printf 'seal "static" {\n  current_key_id = "%s"\n  current_key = "file:///run/secrets/ecpds-openbao-seal.key"\n}\n' "$seal_id" \
            > /run/ecpds/openbao-seal/config.hcl
        chown ecpds-openbao:ecpds-openbao /run/ecpds/openbao-seal/config.hcl
        chmod 0600 /run/ecpds/openbao-seal/config.hcl
        unset seal_id seal_mode
    elif [ -e "$seal_marker" ]; then
        echo "[standalone] This vault requires auto-unseal; changing seal mode is not supported." >&2
        exit 1
    fi
    if [ "$openbao_mode" = embedded ]; then
        install -d -o root -g root -m 0700 /data/lib/openbao-operator
    fi
    install -d -o ecpds-master -g ecpds-master -m 0700 /data/lib/master/openbao /run/ecpds/openbao
    rm -f /run/ecpds/openbao/ready
    cert=/data/lib/openbao/tls/server.crt
    key=/data/lib/openbao/tls/server.key
    if [ ! -e "$cert" ] && [ ! -e "$key" ]; then
        umask 077
        openssl req -x509 -newkey rsa:3072 -nodes -days 3650 \
            -keyout "$key" -out "$cert" -subj '/CN=localhost' \
            -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1' >/dev/null 2>&1
        umask 022
    fi
    if [ ! -f "$cert" ] || [ ! -f "$key" ]; then
        echo "[standalone] Incomplete embedded OpenBao TLS configuration." >&2
        exit 1
    fi
    chown ecpds-openbao:ecpds-openbao "$key" "$cert"
    chmod 0600 "$key"
    chmod 0644 "$cert"
    ca_source=$cert
    openbao_address=https://127.0.0.1:8200
    openbao_token=/data/lib/master/openbao/token
elif [ "$openbao_mode" = true ]; then
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
    openbao_address=https://openbao:8200
    openbao_token=/run/ecpds/openbao/token
elif [ "$openbao_mode" != false ]; then
    echo "[standalone] ECPDS_OPENBAO_ENABLED must be manual, auto, embedded, true, or false." >&2
    exit 1
fi

if [ "$openbao_mode" != false ]; then
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
    awk -v address="$openbao_address" -v token="$openbao_token" '
        /^\[OpenBao\]$/ {
            in_openbao = 1
            found = 1
            print
            print "enabled=yes"
            print "address=" address
            print "mount=ecpds"
            print "tokenFile=" token
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
    echo "[standalone] OpenBao configured; local-password mode remains in use."
fi

# ── Initialise MariaDB on first run ────────────────────────────────────────────
bootstrap_marker=/data/.standalone-bootstrap-incomplete
if [ -e "$bootstrap_marker" ]; then
    echo "[standalone] Database bootstrap was interrupted; restore the data directory or repair it before restarting." >&2
    exit 1
fi
encryption_dir=${ECPDS_DATABASE_ENCRYPTION_DIR:-/data/lib/master/database-encryption}
if [[ "$encryption_dir" != /* ]]; then
    echo "[standalone] ECPDS_DATABASE_ENCRYPTION_DIR must be an absolute path." >&2
    exit 1
fi
encryption_config=$encryption_dir/ecmwf.properties
if [ -n "${ECPDS_DATABASE_ENCRYPTION_DIR:-}" ]; then
    if [ ! -f "$encryption_config" ] || [ -L "$encryption_config" ] ||
        [ ! -f "$encryption_dir/key.p12" ] || [ -L "$encryption_dir/key.p12" ] ||
        ! grep -Fxq "keyStore=$encryption_dir/key.p12" "$encryption_config" ||
        ! runuser -u ecpds-master -- test -r "$encryption_dir/key.p12"; then
        echo "[standalone] External database encryption key or configuration is missing, invalid, or unreadable by the Master." >&2
        exit 1
    fi
    for file in "$encryption_config" "$encryption_dir/key.p12"; do
        owner=$(stat -c '%u' "$file")
        mode=$(stat -c '%a' "$file")
        if [ "$owner" != 0 ] || (( (8#$mode & 027) != 0 )); then
            echo "[standalone] External database key files must be root-owned and not group-writable or world-accessible." >&2
            exit 1
        fi
        for service in ecpds-mover ecpds-monitor ecpds-openbao; do
            if runuser -u "$service" -- test -r "$file"; then
                echo "[standalone] External database key files must not be readable by $service." >&2
                exit 1
            fi
        done
    done
    unset file owner mode service
fi
if [ ! -d /data/db/ecpds ]; then
    umask 077
    : > "$bootstrap_marker"
    if [ -z "${ECPDS_DATABASE_ENCRYPTION_DIR:-}" ]; then
        install -d -o root -g ecpds-master -m 0750 "$encryption_dir"
        export ECPDS_DATABASE_KEY_PASSWORD
        ECPDS_DATABASE_KEY_PASSWORD=$(openssl rand -hex 24)
        keytool -genseckey -alias database-encryption -keyalg AES -keysize 256 \
            -storetype PKCS12 -keystore "$encryption_dir/key.p12" \
            -storepass:env ECPDS_DATABASE_KEY_PASSWORD -keypass:env ECPDS_DATABASE_KEY_PASSWORD >/dev/null 2>&1
        printf '[DatabaseEncryption]\nkeyStore=%s/key.p12\nkeyStorePassword=%s\nkeyAlias=database-encryption\n' \
            "$encryption_dir" "$ECPDS_DATABASE_KEY_PASSWORD" > "$encryption_config"
        unset ECPDS_DATABASE_KEY_PASSWORD
        chown root:ecpds-master "$encryption_dir/key.p12" "$encryption_config"
        chmod 0640 "$encryption_dir/key.p12" "$encryption_config"
    fi
    umask 022
fi
if [ ! -f "$encryption_config" ] || [ ! -f "$encryption_dir/key.p12" ]; then
    echo "[standalone] Database encryption key material is missing; restore it from backup." >&2
    exit 1
fi
if ! grep -Fxq "keyStore=$encryption_dir/key.p12" "$encryption_config"; then
    echo "[standalone] Database encryption configuration must reference the key in $encryption_dir." >&2
    exit 1
fi
key_fingerprint_file=/data/lib/standalone/database-key.sha256
key_fingerprint=$(sha256sum "$encryption_dir/key.p12" | cut -d ' ' -f 1)
if [ ! -d /data/db/ecpds ]; then
    install -d -o root -g root -m 0700 /data/lib/standalone
    if [ -e "$key_fingerprint_file" ] || [ -L "$key_fingerprint_file" ]; then
        echo "[standalone] Database encryption key fingerprint already exists; refusing to overwrite it." >&2
        exit 1
    fi
    umask 077
    printf '%s\n' "$key_fingerprint" > "$key_fingerprint_file"
    umask 022
elif [ ! -f "$key_fingerprint_file" ] || [ -L "$key_fingerprint_file" ] ||
    [ "$(<"$key_fingerprint_file")" != "$key_fingerprint" ]; then
    echo "[standalone] Database encryption key does not match this data directory; restore the original key." >&2
    exit 1
fi
unset key_fingerprint
master_config=/usr/local/ecpds/master/conf/ecmwf.properties
awk -v config="$encryption_config" '
    /^\[DatabaseEncryption\]$/ {
        in_encryption = 1
        found = 1
        while ((getline line < config) > 0) print line
        close(config)
        next
    }
    /^\[/ && in_encryption { in_encryption = 0 }
    in_encryption && /^(keyStore|keyStorePassword|keyAlias)=/ { next }
    { print }
    END { if (!found) exit 1 }
' "$master_config" > "${master_config}.standalone" || {
    rm -f "${master_config}.standalone"
    echo "[standalone] Master database encryption configuration section is missing." >&2
    exit 1
}
cat "${master_config}.standalone" > "$master_config"
rm -f "${master_config}.standalone"
chown root:ecpds-master "$master_config"
chmod 0640 "$master_config"

if [ ! -d /data/db/ecpds ]; then
    echo "[standalone] Initialising MariaDB database..."
    mysql_install_db --user=mysql --datadir=/data/db --skip-name-resolve \
        >/dev/null 2>&1 || true

    # Start MariaDB temporarily (no root password yet)
    mysqld_safe --user=mysql --skip-networking &
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

    install -d -o root -g root -m 0700 /data/lib/standalone
    umask 077
    java -Decmwf.properties="$encryption_config" -cp '/usr/local/ecpds/lib/*' \
        ecmwf.ecpds.master.StandaloneInitialAccounts /data/lib/standalone/initial-accounts \
        /data/lib/standalone/initial-accounts.sql
    mysql --socket=/run/mysqld/mysqld.sock -u root ecpds < /data/lib/standalone/initial-accounts.sql
    rm -f /data/lib/standalone/initial-accounts.sql
    while IFS='=' read -r name value; do
        case "$name" in
            hawtio) hawtio_password=$value ;;
            jolokia) jolokia_password=$value ;;
        esac
    done < /data/lib/standalone/initial-accounts
    : "${hawtio_password:?Missing generated Hawtio credential}"
    : "${jolokia_password:?Missing generated Jolokia credential}"
    for service in master mover monitor; do
        install -d -o "ecpds-$service" -g "ecpds-$service" -m 0700 "/data/lib/$service/standalone-secrets"
        printf '%s\n' "$jolokia_password" > "/data/lib/$service/standalone-secrets/jolokia"
        chown "ecpds-$service:ecpds-$service" "/data/lib/$service/standalone-secrets/jolokia"
    done
    printf '%s\n' "$hawtio_password" > /data/lib/monitor/standalone-secrets/hawtio
    chown ecpds-monitor:ecpds-monitor /data/lib/monitor/standalone-secrets/hawtio
    unset hawtio_password jolokia_password name value
    cli_secret=$(openssl rand -base64 32)
    rcc_secret=$(openssl rand -base64 32)
    keystore_password=$(openssl rand -hex 24)
    for service in master mover; do
        printf '%s\n' "$cli_secret" > "/data/lib/$service/standalone-secrets/cli"
        chown "ecpds-$service:ecpds-$service" "/data/lib/$service/standalone-secrets/cli"
    done
    printf '%s\n' "$rcc_secret" > /data/lib/mover/standalone-secrets/rcc
    chown ecpds-mover:ecpds-mover /data/lib/mover/standalone-secrets/rcc
    for service in mover monitor; do
        printf '%s\n' "$keystore_password" > "/data/lib/$service/standalone-secrets/keystore"
        chown "ecpds-$service:ecpds-$service" "/data/lib/$service/standalone-secrets/keystore"
    done
    unset cli_secret rcc_secret keystore_password
    mysql_root_password=$(openssl rand -hex 24)
    mysql_master_password=$(openssl rand -hex 24)
    printf '[client]\nuser=root\npassword=%s\nsocket=/run/mysqld/mysqld.sock\n' "$mysql_root_password" \
        > /data/lib/standalone/mysql-client.cnf
    printf '%s\n' "$mysql_master_password" > /data/lib/master/standalone-secrets/database
    chown ecpds-master:ecpds-master /data/lib/master/standalone-secrets/database
    printf "ALTER USER 'master'@'%%' IDENTIFIED BY '%s';\nALTER USER 'root'@'localhost' IDENTIFIED BY '%s';\n" \
        "$mysql_master_password" "$mysql_root_password" | mysql --socket=/run/mysqld/mysqld.sock -u root
    unset mysql_root_password mysql_master_password
    umask 022
    echo "[standalone] Initial account credentials are in /data/lib/standalone/initial-accounts (root only)."

    # Store only the hash in the database; the initial secret is available to the operator.
    install -d -o root -g root -m 0700 /data/lib/standalone
    api_secret_file=/data/lib/standalone/api-client-secret
    if [ -e "$api_secret_file" ]; then
        echo "[standalone] Initial API client secret already exists; refusing to overwrite it." >&2
        exit 1
    fi
    umask 077
    api_secret=$(openssl rand -hex 24)
    printf '%s\n' "$api_secret" > "$api_secret_file"
    api_hash=$(printf '%s' "$api_secret" | sha256sum | cut -d ' ' -f 1)
    unset api_secret
    umask 022
    printf '%s\n' \
        "INSERT INTO API_CLIENT (APU_ID, APU_SECRET_HASH, APU_COMMENT, APU_ACTIVE, APU_CREATED) VALUES ('api', '$api_hash', 'Initial standalone API client', 1, UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000);" \
        "INSERT INTO API_PERMISSION (APU_ID, APP_PATTERN) VALUES ('api', '.*');" \
        | mysql --defaults-extra-file=/data/lib/standalone/mysql-client.cnf ecpds
    unset api_hash
    echo "[standalone] Initial API client secret is in /data/lib/standalone/api-client-secret (root only)."

    # Remove anonymous users that would block master@% access
    mysql --defaults-extra-file=/data/lib/standalone/mysql-client.cnf -e \
        "DELETE FROM mysql.user WHERE user=''; FLUSH PRIVILEGES;"

    mysqladmin --defaults-extra-file=/data/lib/standalone/mysql-client.cnf shutdown
    wait $INIT_PID 2>/dev/null || true
    sleep 2
    rm -f "$bootstrap_marker"
    echo "[standalone] Database initialised."
fi

if [ ! -f /data/lib/standalone/mysql-client.cnf ] || [ ! -s /data/lib/master/standalone-secrets/database ]; then
    echo "[standalone] Database credentials are missing; restore them from backup." >&2
    exit 1
fi
export ECPDS_DATABASE_PASSWORD
ECPDS_DATABASE_PASSWORD=$(< /data/lib/master/standalone-secrets/database)
awk '
    /^\[DataBase\]$/ { in_database = 1; found = 1; print; next }
    /^\[/ && in_database { in_database = 0 }
    in_database && /^password=/ { print "password=" ENVIRON["ECPDS_DATABASE_PASSWORD"]; changed = 1; next }
    { print }
    END { if (!found || !changed) exit 1 }
' "$master_config" > "${master_config}.standalone" || {
    rm -f "${master_config}.standalone"
    echo "[standalone] Master database configuration is missing." >&2
    exit 1
}
unset ECPDS_DATABASE_PASSWORD
cat "${master_config}.standalone" > "$master_config"
rm -f "${master_config}.standalone"

# ── Hand off to supervisord (becomes PID 1 replacement) ────────────────────────
/usr/local/ecpds/standalone/internal-rmi-tls.sh
echo "[standalone] Starting all OpenECPDS services..."
exec /usr/bin/supervisord -c /etc/supervisord.conf
