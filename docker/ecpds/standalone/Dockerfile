FROM ecpds/java:graalvm

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
ENV KEYSTORE_PASSWORD=s5w8g771
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
RUN dnf -y install mariadb-server lbzip2 zstd nmap mtr supervisor && \
    dnf clean all

# ── Install OpenECPDS RPMs ─────────────────────────────────────────────────────
COPY rpms/ /tmp/rpms/
RUN yum install -d1 -y \
        /tmp/rpms/ecpds-common-*.noarch.rpm \
        /tmp/rpms/ecpds-master-*.rpm \
        /tmp/rpms/ecpds-mover-*.rpm \
        /tmp/rpms/ecpds-monitor-*.noarch.rpm && \
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
    echo 'pidof=/usr/local/ecpds/standalone/pidof-monitor.sh' > /etc/ecpds/default/monitor.cnf

# ── Allow password auth from any host (inside container, monitor→master via RMI) ─
# By default, ECPDS only accepts password authentication when the calling host
# matches the master server's own hostname/IP. Inside the standalone container
# the monitor calls the master via 127.0.0.1 (RMI loopback), which does not
# match the container's Docker-assigned hostname — so we override to allow all.
RUN sed -i 's/^\(\[Server\]\)$/\1\nallowPasswordOn=.*/' \
        /usr/local/ecpds/master/conf/ecmwf.properties

# ── MariaDB datadir → /data/db ────────────────────────────────────────────────
RUN mkdir -p /etc/my.cnf.d && \
    printf '[mysqld]\ndatadir=/data/db\nsocket=/run/mysqld/mysqld.sock\n\n[client]\nsocket=/run/mysqld/mysqld.sock\n' \
        > /etc/my.cnf.d/zz-ecpds-standalone.cnf

# ── Install self-signed SSL certificates (for standalone/eval use) ─────────────
# Real deployments: mount your own certs over /etc/ecpds/{mover,monitor}/
COPY etc/mover/ecpds-mover.pfx /etc/ecpds/mover/ecpds-mover.pfx
COPY etc/monitor/ecpds-monitor.pfx /etc/ecpds/monitor/ecpds-monitor.pfx

# ── Configuration and startup scripts ─────────────────────────────────────────
COPY ecpds.sql /ecpds-init.sql
COPY supervisord.conf /etc/supervisord.conf
COPY entrypoint.sh start-master.sh start-mover.sh start-monitor.sh \
     /usr/local/ecpds/standalone/
RUN chmod +x /usr/local/ecpds/standalone/*.sh

# ── Single persistent volume ───────────────────────────────────────────────────
# Mount /data to persist all service data, logs, and the database across restarts
VOLUME /data

# ── Exposed ports ──────────────────────────────────────────────────────────────
# Master:   9062 (JMX)  9600 (callback)  9640 (ECpds CLI)  9021 (FTP)
# Mover:    7062 (JMX)  7600 (callback)  7080 (HTTP portal) 7443 (HTTPS portal)
#           7022 (SFTP) 7021 (FTP)       1883 (MQTT)        8883 (MQTTS)  7640 (ECproxy)
# Monitor:  8062 (JMX)  8600 (callback)  8080 (HTTP)        8443 (HTTPS)
EXPOSE 9062 9600 9640 9021 \
       7062 7600 7080 7443 7022 7021 1883 8883 7640 \
       8062 8600 8080 8443

ENTRYPOINT ["/usr/local/ecpds/standalone/entrypoint.sh"]
