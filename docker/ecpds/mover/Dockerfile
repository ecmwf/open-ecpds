FROM ecpds/java:openjdk-22.0.1

LABEL maintainer="Laurent.Gougeon@ecmwf.int"

# Container specific
ENV CONTAINER=podman
ENV CONSOLE_LOG_LEVEL=off

# OpenPDS Data-Mover Profile
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

# Add OpenPDS rpms
COPY rpms/ecpds-mover-*.noarch.rpm .

# Install OpenPDS rpms and lbzip2
RUN yum install -d1 -y lbzip2 && \
    yum install -d1 -y ecpds-mover-*.noarch.rpm && \
    yum clean all && rm -f ecpds-*.rpm

# Mountable volumes
VOLUME /var/log/ecpds/mover
VOLUME /var/tmp/ecpds/mover
VOLUME /var/lib/ecpds/mover/mqtt
VOLUME /var/lib/ecpds/mover/data

# Expose the ports
EXPOSE 7062 7600 7080 7443 7640 7021 7022

# Start OpenPDS services
ENTRYPOINT ["/usr/local/ecpds/mover/sh/mover","start"]
