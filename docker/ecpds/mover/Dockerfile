FROM ecpds/java:graalvm

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
ENV KEYSTORE_PASSWORD=s5w8g771
ENV MOVER_TITLE="Personal Data Store (PDS)"
ENV MOVER_COLOR="#0079d3"
ENV OPERATION=yes
ENV PORT_JMX=7062
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
COPY rpms/ecpds-mover-${TAG}.*.rpm .

# Install OpenECPDS rpms and lbzip2
RUN yum install -d1 -y lbzip2 && \
    yum install -d1 -y ecpds-mover-${TAG}.*.rpm && \
    yum clean all && rm -f ecpds-mover-${TAG}.*.rpm

# Mountable volumes
VOLUME /var/log/ecpds/mover
VOLUME /var/tmp/ecpds/mover
VOLUME /var/lib/ecpds/mover/mqtt
VOLUME /var/lib/ecpds/mover/data

# Expose the ports
EXPOSE 7062 7600 7080 7443 7640 7021 7022

# Start OpenECPDS services
ENTRYPOINT ["/usr/local/ecpds/mover/sh/mover","start"]
