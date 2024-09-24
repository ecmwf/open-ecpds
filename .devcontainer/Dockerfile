ARG BUILDPLATFORM=linux/amd64
FROM --platform=${BUILDPLATFORM} mcr.microsoft.com/vscode/devcontainers/base:latest

# Development container for OpenPDS
LABEL maintainer="Laurent.Gougeon@ecmwf.int"

# Install base packages
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive && \
	apt-get -y install --no-install-recommends \
	make rpm binutils joe file apt-transport-https \
	ca-certificates curl ftp gnupg-agent software-properties-common awscli iputils-ping telnet dos2unix

# Configure awscli (s3)
RUN mkdir /root/.aws
COPY .aws-credentials /root/.aws/credentials

# Add Docker's official GPG key:
RUN apt-get update && \
  apt-get install ca-certificates curl && \
  install -m 0755 -d /etc/apt/keyrings && \
  curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc && \
  chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Docker Apt sources:
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

# And update the repository
RUN apt-get update -y

# Install the latest version of Docker
RUN apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Some cleaning
RUN apt-get autoremove -y && apt-get clean -y

# Donwload and install GraalVM
RUN mkdir /usr/lib/jvm && cd /usr/lib/jvm && \
    wget https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-23.0.0/graalvm-community-jdk-23.0.0_linux-x64_bin.tar.gz && \
    tar -xzf graalvm-community-jdk-23.0.0_linux-x64_bin.tar.gz && \
    rm -f graalvm-community-jdk-23.0.0_linux-x64_bin.tar.gz

# Define environment parameters for the JVM
ENV JAVA_HOME=/usr/lib/jvm/graalvm-community-openjdk-23+37.1
ENV PATH=$JAVA_HOME/bin:$PATH

# Create the JDK Class-Data Archive
RUN java -Xshare:dump

# Re-enable disabled algorithms to allow connecting to low security sites
COPY java.security $JAVA_HOME/conf/security/.

# Download and install Ant
RUN cd /usr/local && wget https://www-eu.apache.org/dist/ant/binaries/apache-ant-1.10.14-bin.tar.gz && \
	tar -xzf apache-ant-1.10.14-bin.tar.gz && \
	rm -f apache-ant-1.10.14-bin.tar.gz

# Define environment parameters for Ant
ENV ANT_HOME=/usr/local/apache-ant-1.10.14
ENV PATH=$ANT_HOME/bin:$PATH

# Download and install Maven
RUN cd /usr/local && wget https://dlcdn.apache.org/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.tar.gz && \
	tar -xzf apache-maven-3.9.8-bin.tar.gz && \
	rm -f apache-maven-3.9.8-bin.tar.gz

# Define environment parameters for Maven
ENV MAVEN_HOME=/usr/local/apache-maven-3.9.8
ENV PATH=$MAVEN_HOME/bin:$PATH

# We are in the dev-container
ENV IN_DEV_CONTAINER=true
