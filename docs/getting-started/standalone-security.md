# Standalone TLS Guidance

The [standalone container](standalone.md) is ready to explore locally without
additional certificate setup. This page covers optional certificate
configuration and maintenance for longer-lived or remotely accessible
installations.

## Trusting or replacing the HTTPS certificate

Standalone initially uses self-signed certificates. For connections beyond a
trusted test environment, configure clients to verify a trusted certificate and
hostname instead of bypassing certificate checks.

Sign in as an administrator and open **Administration → Certificates**
(`/do/admin/certificates`). Generate a self-signed certificate for the hostname
clients use, or import a certificate issued by a trusted CA with the required
subject alternative names. Deploy the Monitor certificate to the Data Mover from
the same page. Certificate changes affect client connections and should be
planned in advance.

For a self-signed certificate, download the **public** Monitor certificate from
that page and verify its fingerprint over a trusted, independent channel before
installing it as a trust anchor on clients. For example, after saving it as
`monitor-cert.pem`, use `curl --cacert monitor-cert.pem https://hostname:8443/`.
The hostname in the URL must match a certificate subject alternative name.
Repeat the trust configuration when replacing the certificate; do not distribute
the private key or disable verification as a substitute for trust.

Hawtio is served over HTTPS using the same keystore as the Monitor UI
(`/data/lib/monitor/tls/ecpds-monitor.pfx`), so the browser shows the same
self-signed certificate warning. Hawtio reads the certificate at startup: after
replacing the Monitor certificate, restart Hawtio with
`docker exec standalone supervisorctl restart hawtio`. Set `HAWTIO_TLS_KEYSTORE`
to another PKCS#12 file to use a different certificate, or to `none` to fall
back to plain HTTP.

## Internal service connections

The Master, Monitor and Mover use mutual TLS for their internal RMI callbacks by
default. First startup creates a private internal CA and separate service
identities under `/data`; the CA private key is root-only and each service's
private key is restricted to that service. The RMI trust store contains only
this CA and does not replace the JVM's normal trust store for outbound HTTPS.
The certificates establish membership in the internal CA, not separate
authorization for each service role. These internal ports should not be
published to untrusted networks.

An empty installation can explicitly opt out with `-e ECPDS_INTERNAL_MTLS=false`;
this disables mutual TLS and is not suitable for production. Once initialized
with mutual TLS, the same data directory cannot be restarted with it disabled.
Back up the internal CA and service keys with `/data`. There is not yet a
need to renew individual keys independently: replace the CA and all three
service identities together, with the container stopped. This interrupts the
service and invalidates the old internal identities. Mutual TLS alone does not
enable Host credential rotation.

Monitor the internal CA expiry with:

```bash
docker exec standalone openssl x509 -in /data/lib/standalone/internal-rmi-ca/ca.crt -noout -enddate
```

Before expiry, stop the container cleanly and take and verify a stopped-volume
backup as described in [Backup and recovery](standalone.md#backup-and-recovery).
Then use the **same image build** and data mount to replace the internal
identities. For the bind mount shown in the [start command](standalone.md#start):

```bash
image=$(docker inspect --format '{{.Image}}' standalone)
docker stop -t 180 standalone
```

Take and verify the stopped-volume backup. Then run:

```bash
docker run --rm --network none \
  -v "$(pwd)/ecpds-data:/data" \
  --entrypoint /usr/local/ecpds/standalone/renew-internal-rmi-tls.sh \
  "$image"
```

Only if renewal succeeds, restart the original container:

```bash
docker start standalone
```

Unseal OpenBao again if using manual mode, and verify service health and
transfers. The renewal command retains the previous private material in a
root-only `internal-rmi-renewal.*` directory under `/data/lib/standalone`;
protect it as part of the backup. If renewal fails or is interrupted, do not
remove the incomplete marker or retry against a partially replaced volume:
restore the stopped-volume backup. A second running standalone container is
refused by the data-volume lock.
