ARG BUILDPLATFORM=linux/amd64
FROM --platform=${BUILDPLATFORM} docker.io/library/rockylinux:9.3

LABEL maintainer="Laurent.Gougeon@ecmwf.int"

# Force https only for yum
RUN cd /etc/yum.repos.d && \
    sed -i 's/mirrorlist=/#mirrorlist=/g' * && \
    sed -i 's/#baseurl=http:/baseurl=https:/g' *

# Install required rpms
RUN yum install -d1 -y findutils procps-ng fontconfig urw-fonts net-tools iputils iperf3 iproute wget diffutils epel-release && /usr/bin/crb enable && \
    yum install -d1 -y joe && \
    yum clean all

# Donwload and install GraalVM
RUN mkdir /usr/lib/jvm && cd /usr/lib/jvm && \
        wget https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-22.0.1/graalvm-community-jdk-22.0.1_linux-x64_bin.tar.gz && \
        tar -xzf graalvm-community-jdk-22.0.1_linux-x64_bin.tar.gz && \
        rm -f graalvm-community-jdk-22.0.1_linux-x64_bin.tar.gz

# Define environment parameters for the JVM
ENV JAVA_HOME=/usr/lib/jvm/graalvm-community-openjdk-22.0.1+8.1
ENV PATH=$JAVA_HOME/bin:$PATH

# Create the JDK Class-Data Archive
RUN java -Xshare:dump

# Re-enable disabled algorithms to allow connecting to low security sites
COPY java.security $JAVA_HOME/conf/security/.
