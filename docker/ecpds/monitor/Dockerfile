FROM ecpds/java:graalvm

ARG TAG
ENV TAG=${TAG}

LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS Monitor"

# Container specific
ENV CONTAINER=podman
ENV CONSOLE_LOG_LEVEL=off

# OpenECPDS Monitor Profile
ENV ALLOCATED_MEMORY=1G
ENV EXTERNAL_ADDRESS=0.0.0.0
ENV INTERNAL_ADDRESS=localhost
ENV MASTER_ADDRESS=localhost
ENV KEYSTORE_PASSWORD=s5w8g771
ENV USE_CACHE=no
ENV MASTER_URL_LIST=""
ENV DEFAULT_SOURCE_HOST_ID=""
ENV MONITOR_NICKNAME="PDS"
ENV MONITOR_TITLE="Personal Data Store"
ENV MONITOR_COLOR="#0079d3"
ENV PORT_JMX=8062
ENV PORT_CALLBACK=8600
ENV PORT_HTTPS=8443
ENV PORT_MASTER=9600
ENV LOG_ROLLOVER_SIZE_MAX=100MB
ENV LOG_ROLLOVER_SIZE_KEEP=20
ENV LOG_LEVEL=warn

# Add OpenECPDS rpms
COPY rpms/ecpds-monitor-${TAG}.noarch.rpm .

# Install OpenECPDS rpms
RUN yum install -d1 -y ecpds-monitor-${TAG}.noarch.rpm && \
    yum clean all && rm -f ecpds-monitor-${TAG}.noarch.rpm

# Mountable volumes
VOLUME /var/log/ecpds/monitor
VOLUME /var/tmp/ecpds/monitor
VOLUME /var/lib/ecpds/monitor

# Expose the ports
EXPOSE 8062 8600 8080 8443

# Start OpenECPDS services
ENTRYPOINT ["/usr/local/ecpds/monitor/sh/monitor","start"]
