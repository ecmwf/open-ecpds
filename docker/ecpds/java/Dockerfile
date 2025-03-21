FROM docker.io/library/rockylinux:9.3

ARG SDKMAN_JDK
ENV SDKMAN_JDK=${SDKMAN_JDK}

LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS JDK"

# Force https only for yum
RUN cd /etc/yum.repos.d && \
    sed -i 's/mirrorlist=/#mirrorlist=/g' * && \
    sed -i 's/#baseurl=http:/baseurl=https:/g' *

# Install required rpms
RUN yum install -d1 -y zip unzip findutils procps-ng fontconfig urw-fonts net-tools iputils iperf3 iproute wget diffutils epel-release && /usr/bin/crb enable && \
    yum install -d1 -y joe && \
    yum clean all

# Install GraalVM using SDKMAN
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk install java ${SDKMAN_JDK}"

# Define environment parameters for the JVM
ENV JAVA_HOME=/root/.sdkman/candidates/java/current
ENV PATH=$JAVA_HOME/bin:$PATH

# Create the JDK Class-Data Archive
RUN java -Xshare:dump

# Re-enable disabled algorithms to allow connecting to low security sites
COPY java.security $JAVA_HOME/conf/security/.

# Copy licences and related files
COPY AUTHORS LICENSE.txt NOTICE VERSION licenses.xml .
COPY licenses /licenses
