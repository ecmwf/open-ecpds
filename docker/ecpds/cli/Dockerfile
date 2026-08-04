FROM rockylinux/rockylinux:9.7-minimal

ARG TAG
ENV TAG=${TAG}

LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS ecpds command-line client"

# ── Install ecpds-tools RPM (provides /usr/local/bin/ecpds) ───────────────────
COPY rpms/ /tmp/rpms/
RUN microdnf install -y openssl-libs && \
    rpm -i /tmp/rpms/ecpds-tools-*.rpm && \
    rm -rf /tmp/rpms && \
    microdnf clean all

ENTRYPOINT ["/usr/local/bin/ecpds"]
