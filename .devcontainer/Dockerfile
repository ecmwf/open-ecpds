FROM mcr.microsoft.com/vscode/devcontainers/base:latest

# Development container for OpenECPDS
LABEL maintainer="Laurent.Gougeon@ecmwf.int"

# Install base packages
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive && \
	apt-get -y install --no-install-recommends \
	make rpm binutils joe file apt-transport-https \
	ca-certificates curl ftp gnupg-agent clang-format \
	software-properties-common awscli iputils-ping telnet

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

# SDKMAN Java Development Kit
ENV SDKMAN_JDK=24-graal

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

# Download and install Maven
RUN cd /usr/local && wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz && \
  tar -xzf apache-maven-3.9.9-bin.tar.gz && \
  rm -f apache-maven-3.9.9-bin.tar.gz

# Define environment parameters for Maven
ENV MAVEN_HOME=/usr/local/apache-maven-3.9.9
ENV PATH=$MAVEN_HOME/bin:$PATH

# Set environment variables for Kompose and Kubectl versions
ENV KOMPOSE_VERSION=1.34.0
ENV KUBECTL_VERSION=1.31.1

# Download and install Kompose and Kubectl based on the platform
RUN if [ "$(uname -m)" = "aarch64" ]; then \
    curl -s -L https://github.com/kubernetes/kompose/releases/download/v${KOMPOSE_VERSION}/kompose-linux-arm64 -o /usr/local/bin/kompose; \
    curl -s -L https://dl.k8s.io/release/v${KUBECTL_VERSION}/bin/linux/arm64/kubectl -o /usr/local/bin/kubectl; \
  else \
    curl -s -L https://github.com/kubernetes/kompose/releases/download/v${KOMPOSE_VERSION}/kompose-linux-amd64 -o /usr/local/bin/kompose; \
    curl -s -L https://dl.k8s.io/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl -o /usr/local/bin/kubectl; \
  fi; \
  chmod +x /usr/local/bin/kompose || true && \
  chmod +x /usr/local/bin/kubectl || true

# We are in the dev-container
ENV IN_DEV_CONTAINER=true
