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
# Evaluation-only credentials for local JMX and Hawtio access.
ENV JOLOKIA_USER=jolokia
ENV JOLOKIA_PASSWORD=jolokia2021
ENV HAWTIO_USER=hawtio
ENV HAWTIO_PASSWORD=hawtio2021
ENV HAWTIO_GATEWAY_PORT=8080
# The container's stdout is not rotated, so do not log every Hawtio request there.
ENV HAWTIO_ACCESS_LOG=off
ENV HAWTIO_PRESET_CONNECTIONS="master=http://127.0.0.1:2062/jolokia,monitor=http://127.0.0.1:3062/jolokia,mover=http://127.0.0.1:4062/jolokia"
ENV JAVA_OPTS="-Dhawtio.proxyAllowlist=127.0.0.1,localhost"

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
# Shared secret for the ecpds CLI <-> Master-Server challenge-response authentication
ENV CLI_SHARED_SECRET="dboUz95GH4U8LihvnJPGfBce5/rFJdbYXoZesRXBIyQ="

# ── Mover settings ─────────────────────────────────────────────────────────────
ENV KEYSTORE_PASSWORD=s5w8g771
# Shared secret for the Mover/Proxy REST control channel authentication (independent from CLI_SHARED_SECRET above)
ENV RCC_SHARED_SECRET="2z14vqbu75XB3IRsddH0gztpXHbUGg9e2GaX3jFq3p0="
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

# ── Database settings ──────────────────────────────────────────────────────────
ENV MYSQL_ROOT_PASSWORD=ecpds2020!

# ── Install MariaDB, tools, and supervisord ────────────────────────────────────
RUN dnf -y install mariadb-server lbzip2 zstd nmap mtr supervisor nginx openssl && \
    dnf clean all

# ── Install OpenECPDS RPMs ─────────────────────────────────────────────────────
COPY rpms/ /tmp/rpms/
RUN yum install -d1 -y \
        /tmp/rpms/ecpds-common-*.noarch.rpm \
        /tmp/rpms/ecpds-master-*.rpm \
        /tmp/rpms/ecpds-mover-*.rpm \
        /tmp/rpms/ecpds-monitor-*.noarch.rpm \
        /tmp/rpms/ecpds-tools-*.rpm && \
    yum clean all && rm -rf /tmp/rpms

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

# ── Disable FTP plugin (not needed in standalone) ─────────────────────────────
RUN sed -i 's/^ftp=ecmwf\.common\.ftpd\.FtpPlugin/#ftp=ecmwf.common.ftpd.FtpPlugin/' \
        /usr/local/ecpds/master/conf/ecmwf.properties \
        /usr/local/ecpds/mover/conf/ecmwf.properties

# ── Mover: bind service ports on all interfaces (not loopback only) ───────────
# Plugin listen addresses default to ${Login[externalAddress]} = 'ecpds-mover'
# which resolves to the loopback interface inside the container. Replace with
# 0.0.0.0 so HTTPS, MQTTS, ECproxy etc. are reachable from outside the container.
RUN sed -i \
    's/listenAddress=\${Login\[externalAddress\]}/listenAddress=0.0.0.0/g' \
    /usr/local/ecpds/mover/conf/ecmwf.properties

# ── MariaDB datadir → /data/db ────────────────────────────────────────────────
RUN mkdir -p /etc/my.cnf.d && \
    printf '[mysqld]\ndatadir=/data/db\nsocket=/run/mysqld/mysqld.sock\ncharacter_set_server=utf8mb4\ncollation_server=utf8mb4_unicode_ci\n\n[client]\nsocket=/run/mysqld/mysqld.sock\ndefault_character_set=utf8mb4\n' \
        > /etc/my.cnf.d/zz-ecpds-standalone.cnf

# ── Configuration and startup scripts ─────────────────────────────────────────
COPY ecpds.sql /ecpds-init.sql
COPY hawtio/lib /opt/hawtio/lib
COPY hawtio/HawtioLauncher.java /opt/hawtio/HawtioLauncher.java
COPY hawtio/entrypoint.sh /entrypoint.sh
RUN mkdir -p /opt/hawtio/classes && \
    javac -d /opt/hawtio/classes -cp "/opt/hawtio/lib/*" /opt/hawtio/HawtioLauncher.java && \
    rm /opt/hawtio/HawtioLauncher.java && \
    chmod 0755 /entrypoint.sh
COPY supervisord.conf /etc/supervisord.conf
COPY entrypoint.sh start-master.sh start-mover.sh start-monitor.sh start-hawtio.sh \
     /usr/local/ecpds/standalone/
RUN chmod +x /usr/local/ecpds/standalone/*.sh

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
