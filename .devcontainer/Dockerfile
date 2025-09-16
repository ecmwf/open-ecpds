# Base image: Rocky Linux 9.6
FROM rockylinux/rockylinux:9.6

# Development container for OpenECPDS
LABEL maintainer="Laurent.Gougeon@ecmwf.int"

# Enable EPEL for extra packages
RUN dnf install -y epel-release && dnf update -y

# Install base development tools and utilities
RUN dnf groupinstall -y "Development Tools" && \
    dnf install -y \
        make rpm-build binutils joe file ftp awscli iputils \
        telnet clang-tools-extra gnupg2 ca-certificates sudo which unzip wget tar \
        openssl-devel \
    && dnf clean all

# Configure AWS CLI
RUN mkdir -p /root/.aws
COPY .aws-credentials /root/.aws/credentials

# Install Docker CE (via official repository)
RUN dnf install -y yum-utils && \
    dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo && \
    dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin && \
    dnf clean all

# Install SDKMAN for GraalVM and Java
ENV SDKMAN_DIR=/root/.sdkman
ENV SDKMAN_JDK=24.0.2-graal

RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && sdk install java ${SDKMAN_JDK}" && \
    bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && sdk install maven"

# Set Java environment variables
ENV JAVA_HOME=$SDKMAN_DIR/candidates/java/current
ENV PATH=$JAVA_HOME/bin:$PATH

# Create the JDK Class-Data Archive
RUN java -Xshare:dump

# Re-enable disabled algorithms to allow connecting to low security sites
COPY java.security $JAVA_HOME/conf/security/

# Install Kompose and kubectl
ENV KOMPOSE_VERSION=1.37.0
ENV KUBECTL_VERSION=1.37.2

RUN if [ "$(uname -m)" = "aarch64" ]; then \
        curl -s -L https://github.com/kubernetes/kompose/releases/download/v${KOMPOSE_VERSION}/kompose-linux-arm64 -o /usr/local/bin/kompose; \
        curl -s -L https://dl.k8s.io/release/v${KUBECTL_VERSION}/bin/linux/arm64/kubectl -o /usr/local/bin/kubectl; \
    else \
        curl -s -L https://github.com/kubernetes/kompose/releases/download/v${KOMPOSE_VERSION}/kompose-linux-amd64 -o /usr/local/bin/kompose; \
        curl -s -L https://dl.k8s.io/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl -o /usr/local/bin/kubectl; \
    fi && \
    chmod +x /usr/local/bin/kompose /usr/local/bin/kubectl

# Set VS Code dev container environment variable
ENV IN_DEV_CONTAINER=true

# Install Node.js (via nvm) and Gemini CLI
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash && \
    export NVM_DIR="/root/.nvm" && \
    . "$NVM_DIR/nvm.sh" && \
    nvm install 22 && \
    nvm use 22 && \
    npm install -g @google/gemini-cli

# Optional: clean up DNF caches
RUN dnf clean all
