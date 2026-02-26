FROM rockylinux/rockylinux:9.6

ARG SDKMAN_JDK
ENV SDKMAN_JDK=${SDKMAN_JDK}
LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS JDK"

# Make DNF more resilient
RUN echo "timeout=300" >> /etc/dnf/dnf.conf && \
    echo "retries=10" >> /etc/dnf/dnf.conf

# Force HTTPS in all repos
RUN sed -i 's|http://|https://|g' /etc/yum.repos.d/*.repo

# Enable EPEL and install required packages
RUN dnf -y install epel-release && \
    dnf -y install \
        zip unzip findutils procps-ng fontconfig urw-base35-fonts \
        net-tools iputils iperf3 iproute wget diffutils joe || \
    dnf -y install --nobest zip unzip findutils procps-ng fontconfig urw-base35-fonts \
        net-tools iputils iperf3 iproute wget diffutils joe && \
    dnf clean all

# Install GraalVM via SDKMAN
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install java $SDKMAN_JDK < /dev/null"

# Define environment parameters for the JVM
ENV JAVA_HOME=/root/.sdkman/candidates/java/current
ENV PATH=$JAVA_HOME/bin:$PATH

# Create the JDK Class-Data Archive
RUN java -Xshare:dump

# Re-enable disabled algorithms to allow connecting to low security sites
COPY java.security $JAVA_HOME/conf/security/

# Copy licences and related files
COPY AUTHORS LICENSE.txt NOTICE VERSION licenses.xml .
COPY licenses /licenses
