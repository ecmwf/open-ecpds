# Standalone OpenBao credential rotation

This advanced, optional setup uses a separate OpenBao container with standalone.
It requires the repository, Docker Compose and OpenSSL. The ordinary
single-container [`docker run` quick start](../getting-started/standalone.md)
instead starts an embedded evaluation vault that unseals automatically by default.

Standalone uses configured Host credentials by default. This alternative
uses a separate OpenBao container with persistent Raft storage and
TLS; OpenBao has no published host port and is reachable only over the Compose
network. The standalone Master runs under its own Linux identity. Mover and
Monitor use separate identities; Hawtio shares Monitor's identity to access its
TLS keystore. None can read the Master token or inspect
the Master process environment/file descriptors. The root-owned supervisor and
container administrator remain trusted.

The standalone authentication flow uses local-password reauthentication only;
standalone does not provide the identity-provider MFA option. Enabling this
backend does **not** enable credential rotation by itself. Configure the
[rotation prerequisites and feature settings](kubernetes.md#credential-rotation-enablement)
on the Master, Monitor and participating Movers before enabling the action.

1. Build the optional OpenBao image and standalone image. From the repository's
   `docker` directory, stage the normal standalone inputs and build
   `open-ecpds/openbao:2.7.2` and the desired standalone image tag:

   ```bash
   make get-rpms
   make build-openbao ARCH=amd64
   make build-sa ARCH=amd64
   ```

   Use the local standalone tag produced by `build-sa` in `STANDALONE_IMAGE`,
   or select the matching GHCR tag. Use a Java 25 base image when building
   the standalone image from the OpenECPDS RPMs.
2. From `docker/ecpds/standalone`, start the vault with the local launcher:

   ```bash
   bash openbao/standalone-openbao-start.sh
   ```

   The launcher uses OpenSSL to generate the development CA and server certificate
   on first use and may prompt for `sudo` to set the server key's ownership.
   It reuses existing `tls.crt`, `tls.key`, and `ca.crt` in `openbao/tls/` without
   replacing them. Keep `openbao/operator/ca.key` offline and out of the
   containers. For a shared environment, supply a certificate issued by the site's
   trusted CA and grant the vault's UID 100 read access to the private key.
3. Initialize the vault once. Protect the initialization output
   as recovery material; split and store the five key shares separately offline.
   Never put these keys or the initial root token in Compose environment variables,
   command-line arguments, image layers, or the Raft volume.

   ```bash
   umask 077
   ./openbao/standalone-openbao-control.sh init > openbao/operator/init-response.json
   ```

   Enter three distinct shares when prompted by `unseal` (run it three times):

   ```bash
   ./openbao/standalone-openbao-control.sh unseal
   ```

4. Configure the KV v2 mount and Master policy, then issue a 30-day,
   non-renewable Master token. Both commands prompt for the root token without
   echoing it. Store the generated application token with the protected helper:

   ```bash
   ./openbao/standalone-openbao-control.sh bootstrap
   ./openbao/standalone-openbao-control.sh issue-token
   ./openbao/standalone-openbao-save-token.sh
   ```

   The token file is created mode `0600`; keep it outside version control and
   replace it before expiry. Before revoking the initial root token, establish
   and test a separate, securely stored administrative login capable of issuing
   replacement application tokens or authorizing root-token generation. This
   external-vault setup does not provision that login. Do not rely on the
   application token or unseal shares alone for administration.

   Once the administrative recovery path is verified, revoke the initial root
   token and remove the local initialization response after separately storing
   the unseal shares:

   ```bash
   ./openbao/standalone-openbao-control.sh revoke-root
   ```

   To replace the application token, use site administration tools with the
   administrative login, or authorize generation of a temporary root token,
   provide three unseal shares, and use the `issue-token` helper with that token.
   Revoke any temporary root token after issuing the replacement. Remove the old
   token file, use the helper to save its replacement, then recreate the container to
   remount the new file and copy it into the Master-only `/run` path:

   ```bash
   docker compose -f compose.openbao.yml up -d --force-recreate standalone
   ```
5. Start the standalone service using the opt-in Compose file:

   ```bash
   STANDALONE_IMAGE=ghcr.io/ecmwf/open-ecpds/standalone:latest \
     docker compose -f compose.openbao.yml up -d standalone
   ```

The application container is named `standalone-openbao-app`. Retrieve its generated
initial account credentials with:

```bash
docker exec standalone-openbao-app cat /data/lib/standalone/initial-accounts
```

Protect and record the credentials, then remove the root-only file. For the API
secret and access details, see [standalone access](../getting-started/standalone.md#access),
using `standalone-openbao-app` in place of `standalone` in `docker exec` commands.
The Compose file does not publish the CLI's Master and Mover ports; configure
both securely if needed, as described in the [CLI guide](../getting-started/ecpds-cli.md#connecting-from-another-machine).

The entrypoint installs only the public CA certificate into the Java trust store
and copies the protected token to a `0600` runtime file owned by the Master.
The external OpenBao data survives restarts
and `docker compose down`; do not use `down -v` unless you intend to erase it.
After a host/container restart, OpenBao remains sealed and requires the operator
to enter the threshold of offline shares again. There is deliberately no
auto-unseal key stored beside the encrypted Raft data.

If the initial root token is lost, OpenBao root-token generation requires both
an authorized administrative login and the required offline shares. If either
is unavailable, that recovery path cannot be used. If the shares are lost,
restore both an encrypted Raft backup and its separately protected recovery
material; the data volume alone is not a recovery backup. Revoke
expired/replaced application tokens through OpenBao after confirming the Master
is using the replacement.

For automated regression commands, see
[Credential rotation checks](../contributing.md#credential-rotation-checks).
