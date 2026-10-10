FROM open-ecpds/java:graalvm

LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS Hawtio (JMX web console for Jolokia)"

RUN dnf -y install nginx openssl && dnf clean all

# Hawtio and its runtime dependencies (fetched by 'mvn package -pl ecpds-core', staged into
# this build context by 'make get-hawtio' - see docs/monitoring/jmx-export.md). Not the
# official project's own distribution: there isn't one (Hawtio is distributed via JBang,
# which has quoting issues with JSON system property values - see HawtioLauncher.java).
COPY lib /opt/hawtio/lib
COPY HawtioLauncher.java /opt/hawtio/
RUN javac -d /opt/hawtio/classes -cp "/opt/hawtio/lib/*" /opt/hawtio/HawtioLauncher.java && \
    rm /opt/hawtio/HawtioLauncher.java

# Jetty is private; nginx exposes the authenticated gateway on port 8080.

# Optional: comma-separated name=url pairs pre-filling the Connect/Remote page, e.g.
# "master=http://localhost:2062/jolokia,mover=http://localhost:4062/jolokia". Note: this
# also auto-opens each one as a browser tab on every fresh visit to the Hawtio home page
# (upstream behaviour, not something this image controls) - see docs/monitoring/jmx-export.md.
ENV HAWTIO_PRESET_CONNECTIONS=""

# Extra JVM options, e.g. -Dhawtio.proxyAllowlist=host1,host2
ENV JAVA_OPTS=""

# The container's stdout is not rotated, so do not log every Hawtio request there.
# Set to /dev/stdout or an absolute file path to re-enable the gateway access log.
ENV HAWTIO_ACCESS_LOG=off

COPY entrypoint.sh /entrypoint.sh
RUN chmod 0755 /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
