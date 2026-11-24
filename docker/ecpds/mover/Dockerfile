FROM open-ecpds/java:graalvm

ARG TAG
ENV TAG=${TAG}

LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS Data Mover"

# Container specific
ENV CONTAINER=podman
ENV CONSOLE_LOG_LEVEL=off

# OpenECPDS Data-Mover Profile
ENV ALLOCATED_MEMORY=1G
ENV EXTERNAL_ADDRESS=0.0.0.0
ENV INTERNAL_ADDRESS=localhost
ENV MASTER_ADDRESS=localhost
ENV KEYSTORE_PASSWORD=""
ENV RCC_SHARED_SECRET=""
ENV CLI_SHARED_SECRET=""
ENV MOVER_TITLE="Personal Data Store (PDS)"
ENV MOVER_COLOR="#0079d3"
ENV OPERATION=yes
# Optional JMX export (see docs/monitoring/jmx-export.md) - unset by default,
# leaving no JMX agent attached.
ENV USE_JCONSOLE=""
ENV JOLOKIA_PORT=""
ENV JOLOKIA_HOST=""
ENV JOLOKIA_USER=""
ENV JOLOKIA_PASSWORD=""
ENV PROMETHEUS_JMX_PORT=""
ENV PROMETHEUS_JMX_HOST=""
ENV PORT_CALLBACK=7600
ENV PORT_HTTP=80
ENV PORT_HTTPS=7443
ENV PORT_MQTT=1883
ENV PORT_MQTTS=8883
ENV PORT_ECPROXY=7640
ENV PORT_FTP=7021
ENV PORT_SSH=7022
ENV PORT_MASTER=9600
ENV LOG_ROLLOVER_SIZE_MAX=100MB
ENV LOG_ROLLOVER_SIZE_KEEP=20
ENV LOG_LEVEL=warn

# Add OpenECPDS rpms
COPY rpms/ecpds-common-${TAG}.noarch.rpm rpms/ecpds-mover-${TAG}.*.rpm ./

# Install OpenECPDS rpms, lbzip2, zstd, nmap and mtr
RUN for package in ./*.rpm; do \
        rpm -qpl "$package" > /tmp/ecpds-rpm-files || exit 1; \
        if grep -Eq '(^|/)id_(rsa|dsa|ecdsa|ed25519)(\.pub)?$|\.(key|pem|p12|pfx|jks|keystore)$' /tmp/ecpds-rpm-files; then \
            echo "Refusing RPM containing key or keystore material: $package" >&2; exit 1; \
        fi; \
    done && rm /tmp/ecpds-rpm-files && \
    yum install -d1 -y lbzip2 zstd nmap mtr && \
    yum install -d1 -y ecpds-common-${TAG}.noarch.rpm ecpds-mover-${TAG}.*.rpm && \
    yum clean all && rm -f ecpds-common-${TAG}.noarch.rpm ecpds-mover-${TAG}.*.rpm

# Mountable volumes
VOLUME /var/log/ecpds/mover
VOLUME /var/tmp/ecpds/mover
VOLUME /var/lib/ecpds/mover/mqtt
VOLUME /var/lib/ecpds/mover/data

# Expose the ports
EXPOSE 7600 7080 7443 7640 7021 7022

# Start OpenECPDS services
ENTRYPOINT ["/usr/local/ecpds/mover/sh/mover","start"]
