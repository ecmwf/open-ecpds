ARG BUILDPLATFORM=linux/amd64
FROM --platform=${BUILDPLATFORM} ecpds/java:openjdk-23.0.0

ARG TAG
ENV TAG=${TAG}

LABEL maintainer="Laurent.Gougeon@ecmwf.int"

# Container specific
ENV CONTAINER=podman
ENV CONSOLE_LOG_LEVEL=off

# OpenECPDS Master-Server Profile
ENV ALLOCATED_MEMORY=1G
ENV EXTERNAL_ADDRESS=0.0.0.0
ENV INTERNAL_ADDRESS=localhost
ENV ECACCESS_ADDRESS=""
ENV ECCMD_ADDRESS=""
ENV DATABASE_ALIAS="sequential://localhost/ecpds"
ENV DATABASE_MIN_SPOOL_SIZE=5
ENV DATABASE_MAX_SPOOL_SIZE=50
ENV RETRIEVAL_MAX_SPOOL_SIZE=50
ENV METADATA_MISSING_CONTACTS_MAIL=""
ENV SMTP_HOST=""
ENV SMTP_FROM=""
ENV OPSVIEW_HOST=""
ENV PORT_JMX=9062
ENV PORT_CALLBACK=9600
ENV PORT_ECPDS=9640
ENV PORT_FTP=9021
ENV LOG_ROLLOVER_SIZE_MAX=100MB
ENV LOG_ROLLOVER_SIZE_KEEP=20
ENV LOG_ROLLOVER_DATE_PATTERN=dd-MM-yyyy
ENV LOG_ROLLOVER_DATE_KEEP=10d
ENV LOG_LEVEL=warn

# Add OpenECPDS rpms
COPY rpms/ecpds-master-${TAG}.noarch.rpm .

# Install OpenECPDS rpms
RUN yum install -d1 -y ecpds-master-${TAG}.noarch.rpm && \
	yum clean all && rm -f ecpds-master-${TAG}.noarch.rpm

# Mountable volumes
VOLUME /var/log/ecpds/master
VOLUME /var/tmp/ecpds/master
VOLUME /var/lib/ecpds/master

# Expose the ports
EXPOSE 9062 9600 9640 9021

# Start OpenECPDS services
ENTRYPOINT ["/usr/local/ecpds/master/sh/master","start"]
