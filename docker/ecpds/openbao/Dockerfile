FROM docker.io/openbao/openbao:2.7.2@sha256:5046911075e137e154ce49ab051502f4ec7cf367d484ae4a4dce665d9b90561f

LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS OpenBao credential vault (single-node deployment)"

COPY openbao.hcl /openbao/config/ecpds.hcl
COPY master-policy.hcl /openbao/policies/master-policy.hcl
COPY --chmod=0755 ecpds-openbao-bootstrap.sh ecpds-openbao-issue-token.sh \
    ecpds-openbao-revoke-root.sh /usr/local/bin/

USER 100:1000
ENTRYPOINT ["/usr/bin/bao"]
CMD ["server", "-config=/openbao/config/ecpds.hcl"]
