FROM docker.io/openbao/openbao:2.7.2@sha256:5046911075e137e154ce49ab051502f4ec7cf367d484ae4a4dce665d9b90561f AS openbao
FROM open-ecpds/java:graalvm

ARG TAG
ENV TAG=${TAG}

LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS Standalone (all-in-one)"

# ── Shared settings ────────────────────────────────────────────────────────────
ENV CONTAINER=podman
ENV CONSOLE_LOG_LEVEL=warn
ENV ALLOCATED_MEMORY=512M
ENV EXTERNAL_ADDRESS=0.0.0.0
ENV INTERNAL_ADDRESS=localhost
ENV LOG_LEVEL=warn
ENV LOG_ROLLOVER_SIZE_MAX=100MB
ENV LOG_ROLLOVER_SIZE_KEEP=5
ENV LOG_ROLLOVER_DATE_PATTERN=dd-MM-yyyy
ENV LOG_ROLLOVER_DATE_KEEP=5d
# Local JMX and Hawtio identities; passwords are generated on first startup.
ENV JOLOKIA_USER=jolokia
ENV JOLOKIA_POLICY_LOCATION=file:/etc/ecpds/jolokia-access.xml
ENV HAWTIO_USER=hawtio
ENV HAWTIO_GATEWAY_PORT=8080
# The container's stdout is not rotated, so do not log every Hawtio request there.
ENV HAWTIO_ACCESS_LOG=off
ENV HAWTIO_PRESET_CONNECTIONS="master=http://127.0.0.1:2062/jolokia,monitor=http://127.0.0.1:3062/jolokia,mover=http://127.0.0.1:4062/jolokia"
ENV JAVA_OPTS="-Dhawtio.proxyAllowlist=127.0.0.1,localhost"
ENV ECPDS_OPENBAO_ENABLED=embedded
ENV ECPDS_INTERNAL_MTLS=true

# ── Master settings ────────────────────────────────────────────────────────────
ENV DATABASE_ALIAS="sequential://127.0.0.1/ecpds"
ENV DATABASE_MIN_SPOOL_SIZE=5
ENV DATABASE_MAX_SPOOL_SIZE=20
ENV RETRIEVAL_MAX_SPOOL_SIZE=20
ENV ECACCESS_ADDRESS=""
ENV ECCMD_ADDRESS=""
ENV METADATA_MISSING_CONTACTS_MAIL=""
ENV SMTP_HOST=""
ENV SMTP_FROM=""
ENV OPSVIEW_HOST=""
# ── Mover settings ─────────────────────────────────────────────────────────────
ENV MOVER_TITLE="Personal Data Store (PDS)"
ENV MOVER_COLOR="#0079d3"
ENV OPERATION=yes

# ── Monitor settings ───────────────────────────────────────────────────────────
ENV USE_CACHE=no
ENV MASTER_URL_LIST=""
ENV DEFAULT_SOURCE_HOST_ID=""
ENV MONITOR_NICKNAME="PDS"
ENV MONITOR_TITLE="Personal Data Store"
ENV MONITOR_COLOR="#0079d3"

# ── Install MariaDB, tools, and supervisord ────────────────────────────────────
RUN dnf -y install mariadb-server lbzip2 zstd nmap mtr supervisor nginx openssl jq && \
    dnf clean all
COPY --from=openbao /usr/bin/bao /usr/local/bin/bao
COPY --from=openbao /licenses/mozilla.txt /usr/share/licenses/openbao/mozilla.txt

# The base image's SDKMAN installation is below /root and cannot be traversed
# by the isolated service identities. Keep the same JDK in an accessible path.
RUN mkdir -p /opt/ecpds-java && cp -a "$JAVA_HOME"/. /opt/ecpds-java/ && \
    chmod -R a+rX /opt/ecpds-java
ENV JAVA_HOME=/opt/ecpds-java
ENV PATH=/opt/ecpds-java/bin:${PATH}

# ── Install OpenECPDS RPMs ─────────────────────────────────────────────────────
COPY rpms/ /tmp/rpms/
RUN for package in /tmp/rpms/*.rpm; do \
        if rpm -qpl "$package" | grep -Eq '(^|/)id_(rsa|dsa|ecdsa|ed25519)(\.pub)?$|\.(key|pem|p12|pfx|jks|keystore)$'; then \
            echo "Refusing RPM containing key or keystore material: $package" >&2; exit 1; \
        fi; \
    done && \
    yum install -d1 -y \
        /tmp/rpms/ecpds-common-*.noarch.rpm \
        /tmp/rpms/ecpds-master-*.rpm \
        /tmp/rpms/ecpds-mover-*.rpm \
        /tmp/rpms/ecpds-monitor-*.noarch.rpm \
        /tmp/rpms/ecpds-tools-*.rpm && \
    awk '\
        /^\[/ { section = $0 } \
        (section == "[DataBase]" || section == "[OpsViewManager]") && /^[[:space:]]*password[[:space:]]*=/ { \
            count++; if ($0 !~ /^[[:space:]]*password[[:space:]]*=[[:space:]]*$/) invalid = 1 \
        } \
        END { exit (count != 2 || invalid) }' /usr/local/ecpds/master/conf/ecmwf.properties || \
        { echo "Refusing Master RPM with missing or embedded passwords" >&2; exit 1; } && \
    yum clean all && rm -rf /tmp/rpms

# Verify the embedded compiler can initialize against the packaged Truffle libraries.
RUN printf '%s\n' \
        'import org.graalvm.polyglot.Context;' \
        'class ScriptSmokeTest {' \
        '  public static void main(String[] args) {' \
        '    try (var context = Context.newBuilder("js").build()) {' \
        '      if (context.eval("js", "1 + 1").asInt() != 2) throw new AssertionError();' \
        '    }' \
        '  }' \
        '}' > /tmp/ScriptSmokeTest.java && \
    java --class-path '/usr/local/ecpds/lib/*' /tmp/ScriptSmokeTest.java && \
    rm /tmp/ScriptSmokeTest.java

# ── Redirect all runtime data to /data (single mountable volume) ──────────────
# Patch ecenv files so services write logs/tmp/data under /data
RUN sed -i \
        -e 's|ECPDS_LOG="/var/log/ecpds"|ECPDS_LOG="/data/log"|g' \
        -e 's|ECPDS_TMP="/var/tmp/ecpds"|ECPDS_TMP="/data/tmp"|g' \
        -e 's|ECPDS_DATA="/var/lib/ecpds"|ECPDS_DATA="/data/lib"|g' \
        /usr/local/ecpds/master/sh/ecenv && \
    sed -i \
        -e 's|ECPDS_LOG="/var/log/ecpds"|ECPDS_LOG="/data/log"|g' \
        -e 's|ECPDS_TMP="/var/tmp/ecpds"|ECPDS_TMP="/data/tmp"|g' \
        -e 's|ECPDS_LIB="/var/lib/ecpds"|ECPDS_LIB="/data/lib"|g' \
        /usr/local/ecpds/mover/sh/ecenv && \
    sed -i \
        -e 's|ECPDS_LOG="/var/log/ecpds"|ECPDS_LOG="/data/log"|g' \
        -e 's|ECPDS_TMP="/var/tmp/ecpds"|ECPDS_TMP="/data/tmp"|g' \
        /usr/local/ecpds/monitor/sh/ecenv

# ── Per-service pidof wrappers (avoids cross-service false positives) ──────────
# In container mode the startup scripts use `pidof java` to detect a running
# instance — in a multi-service container all three Java processes match.
# Replace with service-specific pgrep wrappers that key on a unique JVM flag.
RUN mkdir -p /etc/ecpds/default /usr/local/ecpds/standalone && \
    find /usr/local/ecpds -type d -exec chmod 0755 {} + && \
    printf '#!/bin/sh\npgrep -f "Dmaster.log"\n' > /usr/local/ecpds/standalone/pidof-master.sh && \
    printf '#!/bin/sh\npgrep -f "Dmover.log"\n'  > /usr/local/ecpds/standalone/pidof-mover.sh  && \
    printf '#!/bin/sh\npgrep -f "Dmonitor.log"\n' > /usr/local/ecpds/standalone/pidof-monitor.sh && \
    chmod +x /usr/local/ecpds/standalone/pidof-*.sh && \
    echo 'pidof=/usr/local/ecpds/standalone/pidof-master.sh'  > /etc/ecpds/default/master.cnf  && \
    echo 'pidof=/usr/local/ecpds/standalone/pidof-mover.sh'   > /etc/ecpds/default/mover.cnf   && \
    echo 'pidof=/usr/local/ecpds/standalone/pidof-monitor.sh' > /etc/ecpds/default/monitor.cnf && \
    printf 'JOLOKIA_PORT=2062\nJOLOKIA_HOST=127.0.0.1\nUSE_JCONSOLE=true\nPROMETHEUS_JMX_PORT=9404\n' \
        >> /etc/ecpds/default/master.cnf && \
    printf 'JOLOKIA_PORT=4062\nJOLOKIA_HOST=127.0.0.1\nUSE_JCONSOLE=true\nPROMETHEUS_JMX_PORT=9405\n' \
        >> /etc/ecpds/default/mover.cnf && \
    printf 'JOLOKIA_PORT=3062\nJOLOKIA_HOST=127.0.0.1\nUSE_JCONSOLE=true\nPROMETHEUS_JMX_PORT=9406\n' \
        >> /etc/ecpds/default/monitor.cnf

# ── Allow password auth from any host (inside container, monitor→master via RMI) ─
# By default, ECPDS only accepts password authentication when the calling host
# matches the master server's own hostname/IP. Inside the standalone container
# the monitor calls the master via 127.0.0.1 (RMI loopback), which does not
# match the container's Docker-assigned hostname — so we override to allow all.
RUN sed -i 's/^\(\[Server\]\)$/\1\nallowPasswordOn=.*/' \
        /usr/local/ecpds/master/conf/ecmwf.properties

# Older staged RPMs may still contain the former shared standalone API client.
RUN sed -i '/^api=api2021:\.\*$/d' /usr/local/ecpds/master/conf/ecmwf.properties

# ── Disable FTP plugin (not needed in standalone) ─────────────────────────────
RUN sed -i 's/^ftp=ecmwf\.common\.ftpd\.FtpPlugin/#ftp=ecmwf.common.ftpd.FtpPlugin/' \
        /usr/local/ecpds/master/conf/ecmwf.properties \
        /usr/local/ecpds/mover/conf/ecmwf.properties

# Keep the vault client disabled by default even if the packaged Master
# configuration predates the OpenBao section.
RUN if ! grep -q '^\[OpenBao\]$' /usr/local/ecpds/master/conf/ecmwf.properties; then \
        printf '\n[OpenBao]\nenabled=no\naddress=https://openbao:8200\nmount=ecpds\ntokenFile=/run/ecpds/openbao/token\n' \
            >> /usr/local/ecpds/master/conf/ecmwf.properties; \
    fi && \
    if ! grep -q '^\[DatabaseEncryption\]$' /usr/local/ecpds/master/conf/ecmwf.properties; then \
        printf '\n[DatabaseEncryption]\n' >> /usr/local/ecpds/master/conf/ecmwf.properties; \
    fi

# ── Mover: bind service ports on all interfaces (not loopback only) ───────────
# Plugin listen addresses default to ${Login[externalAddress]} = 'ecpds-mover'
# which resolves to the loopback interface inside the container. Replace with
# 0.0.0.0 so HTTPS, MQTTS, ECproxy etc. are reachable from outside the container.
RUN sed -i \
    's/listenAddress=\${Login\[externalAddress\]}/listenAddress=0.0.0.0/g' \
    /usr/local/ecpds/mover/conf/ecmwf.properties
RUN sed -i 's|^keyPairsDir=.*|keyPairsDir=/data/lib/mover/ssh/keys|' \
        /usr/local/ecpds/mover/conf/ecmwf.properties && \
    grep -qx 'keyPairsDir=/data/lib/mover/ssh/keys' /usr/local/ecpds/mover/conf/ecmwf.properties
RUN sed -i 's|SSLKeyStore=${monitor.etc}/ecpds-monitor.pfx|SSLKeyStore=/data/lib/monitor/tls/ecpds-monitor.pfx|' \
        /usr/local/ecpds/monitor/conf/ecmwf.properties && \
    sed -i 's|SSLKeyStore=${mover.etc}/ecpds-mover.pfx|SSLKeyStore=/data/lib/mover/tls/ecpds-mover.pfx|' \
        /usr/local/ecpds/mover/conf/ecmwf.properties

# ── MariaDB datadir → /data/db ────────────────────────────────────────────────
RUN mkdir -p /etc/my.cnf.d && \
    printf '[mysqld]\ndatadir=/data/db\nsocket=/run/mysqld/mysqld.sock\nbind-address=127.0.0.1\ncharacter_set_server=utf8mb4\ncollation_server=utf8mb4_unicode_ci\n\n[client]\nsocket=/run/mysqld/mysqld.sock\ndefault_character_set=utf8mb4\n' \
        > /etc/my.cnf.d/zz-ecpds-standalone.cnf

# ── Isolate service processes ──────────────────────────────────────────────────
# A single container normally runs all services as root. The optional OpenBao
# integration needs the Master token to be readable by the Master and nobody else.
RUN groupadd --system --gid 12001 ecpds-master && \
    groupadd --system --gid 12002 ecpds-mover && \
    groupadd --system --gid 12003 ecpds-monitor && \
    groupadd --system --gid 12005 ecpds-openbao && \
    useradd --system --uid 12001 --gid ecpds-master --no-create-home --shell /sbin/nologin ecpds-master && \
    useradd --system --uid 12002 --gid ecpds-mover --no-create-home --shell /sbin/nologin ecpds-mover && \
    useradd --system --uid 12003 --gid ecpds-monitor --no-create-home --shell /sbin/nologin ecpds-monitor && \
    useradd --system --uid 12005 --gid ecpds-openbao --no-create-home --shell /sbin/nologin ecpds-openbao

# ── Configuration and startup scripts ─────────────────────────────────────────
COPY ecpds.sql /ecpds-init.sql
COPY hawtio/lib /opt/hawtio/lib
COPY hawtio/HawtioLauncher.java /opt/hawtio/HawtioLauncher.java
COPY hawtio/entrypoint.sh /entrypoint.sh
COPY jolokia-access.xml /etc/ecpds/jolokia-access.xml
RUN mkdir -p /opt/hawtio/classes && \
    javac -d /opt/hawtio/classes -cp "/opt/hawtio/lib/*" /opt/hawtio/HawtioLauncher.java && \
    rm /opt/hawtio/HawtioLauncher.java && \
    sed -i -e '/^user nginx;$/d' \
        -e 's#error_log /dev/stderr;#error_log /run/hawtio/nginx-error.log;#' \
        -e 's#chmod 0644 /run/hawtio/htpasswd#chmod 0600 /run/hawtio/htpasswd#' \
        -e 's#chmod 0755 /run/hawtio$#chmod 0700 /run/hawtio#' /entrypoint.sh && \
    chmod 0755 /entrypoint.sh
COPY supervisord.conf /etc/supervisord.conf
COPY entrypoint.sh check-data-version.sh data-lock.sh healthcheck.sh internal-rmi-tls.sh renew-internal-rmi-tls.sh start-master.sh start-mover.sh start-monitor.sh start-hawtio.sh start-openbao.sh openbao-operator.sh \
     /usr/local/ecpds/standalone/
COPY openbao.hcl openbao-policy.hcl /etc/ecpds/
COPY Dockerfile /tmp/standalone-Dockerfile
RUN chmod +x /usr/local/ecpds/standalone/*.sh && \
    mkdir -p /run/ecpds/openbao /etc/pki/ca-trust/source/anchors && \
    chown ecpds-master:ecpds-master /run/ecpds/openbao && \
    chmod 0700 /run/ecpds/openbao && \
    openssl rand -hex 32 > /etc/ecpds/standalone-build-id && \
    printf '%s\n' "${TAG:-unversioned}" > /etc/ecpds/standalone-release && \
    rm /tmp/standalone-Dockerfile

HEALTHCHECK --interval=30s --timeout=15s --start-period=120s --retries=3 \
    CMD ["/usr/local/ecpds/standalone/healthcheck.sh"]

# ── Single persistent volume ───────────────────────────────────────────────────
# Mount /data to persist all service data, logs, and the database across restarts
VOLUME /data

# ── Exposed ports ──────────────────────────────────────────────────────────────
# Master:   9600 (callback)  9640 (ECpds CLI)  [FTP disabled - see note]
# Mover:    7600 (callback)  7443 (HTTPS portal)
#           7022 (SFTP) 8883 (MQTTS)  7640 (ECproxy)  [FTP disabled - see note]
# Monitor:  8600 (callback)  8443 (HTTPS)
# Hawtio:   8080 (authenticated HTTPS gateway, Monitor certificate)
# Prometheus metrics: 9404 (Master) 9405 (Mover) 9406 (Monitor); bind to localhost when publishing
EXPOSE 9062 9600 9640 \
       7062 7600 7443 7022 8883 7640 \
       8062 8600 8443 \
       8080 9404 9405 9406

ENTRYPOINT ["/usr/local/ecpds/standalone/entrypoint.sh"]
