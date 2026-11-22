# Getting the ecpds CLI

The `ecpds` command-line client is a small native C binary that submits files and
requests to the OpenECPDS Master Server. It is the primary way to push data into
OpenECPDS programmatically.

For a detailed description of the submission workflows (synchronous, asynchronous, ECMWF
retrieval mechanism), see [ECPDS Command-line Tool](../use-cases/ecpds-cli.md).

## Installation options

### Option A — Download a pre-built binary

Pre-compiled binaries for `amd64` (x86\_64) and `arm64` (aarch64) are published as
assets on every [GitHub Release](https://github.com/ecmwf/open-ecpds/releases).

```bash
# Linux amd64
curl -Lo /usr/local/bin/ecpds \
  https://github.com/ecmwf/open-ecpds/releases/latest/download/ecpds-amd64
chmod +x /usr/local/bin/ecpds

# Linux arm64
curl -Lo /usr/local/bin/ecpds \
  https://github.com/ecmwf/open-ecpds/releases/latest/download/ecpds-arm64
chmod +x /usr/local/bin/ecpds
```

The binary requires only `libcrypto` (OpenSSL) — no JVM, no other runtime dependency.

### Option B — Pull the CLI Docker image

A minimal container image is published alongside the other OpenECPDS images:

```bash
docker pull ghcr.io/ecmwf/open-ecpds/cli:latest
```

Use it as a one-shot command (mount the files you want to submit). If the
deployment uses a CLI shared secret, obtain it securely from its administrator
and export `CLI_SHARED_SECRET` before running:

```bash
read -rs -p 'CLI shared secret: ' CLI_SHARED_SECRET; echo
export CLI_SHARED_SECRET
read -rs -p 'Data User password: ' DATA_PASSWORD; echo
docker run --rm \
  -e CLI_SHARED_SECRET \
  -v /path/to/data:/data \
  ghcr.io/ecmwf/open-ecpds/cli:latest \
  -echost master.example.com -ecport 9640 \
  -user mylogin -pass "$DATA_PASSWORD" \
  -destination MY_DESTINATION \
  -source /data/myfile.dat
unset DATA_PASSWORD CLI_SHARED_SECRET
```

For deployments without a shared secret, omit the secret prompt, export and
`-e` option. Ensure the returned Mover hostname is resolvable and reachable
inside the CLI container. Password arguments and container environment variables
can be visible to local administrators or process-inspection tools.

### Option C — Extract from the standalone container

The `ecpds` binary is included in the standalone image and installed at
`/usr/local/bin/ecpds`. You can copy it out of a running or stopped container:

```bash
docker cp standalone:/usr/local/bin/ecpds ./ecpds
chmod +x ./ecpds
```

### Option D — Build from source

Inside the development container, the binary is compiled automatically by `mvn package`
(via the `native-build` profile activated by `IN_DEV_CONTAINER=true`). The output is at
`ecpds-native/target/ecpds`. To stage it as a named release binary:

```bash
make release-tools   # produces release/ecpds-amd64 or release/ecpds-arm64
```

## Using the CLI against the standalone container

The [standalone `docker run` example](standalone.md#start) publishes the Master
port **9640** and Mover port **7640** on the container host's localhost. A file upload
connects to both: the Master returns the Mover address `ecpds-mover:7640`.
On the machine running the native CLI, arrange for `ecpds-mover` to resolve to
`127.0.0.1`, for example with `127.0.0.1 ecpds-mover` in its hosts file. This
name is resolved by the client, not by the standalone container.

The container administrator can retrieve the generated shared secret with
`docker exec standalone cat /data/lib/master/standalone-secrets/cli` and the
initial `test` user password from `/data/lib/standalone/initial-accounts`.
If `CLI_SHARED_SECRET` was explicitly set for the container, use that value
instead of the generated file. Transfer and store both credentials securely.
On the CLI machine, enter them without echoing:

```bash
read -rs -p 'CLI shared secret: ' CLI_SHARED_SECRET; echo
export CLI_SHARED_SECRET
read -rs -p 'Test user password: ' TEST_PASSWORD; echo
```

For example, to submit a local file to the pre-configured `aviso_demo`
destination:

```bash
ecpds \
  -echost localhost -ecport 9640 \
  -user test -pass "$TEST_PASSWORD" \
  -destination aviso_demo \
  -source /path/to/myfile.dat
```

The destination starts stopped; start it in the Monitor if the file should be
delivered. The password passed with `-pass` can be visible in process listings.
Unset `TEST_PASSWORD` and `CLI_SHARED_SECRET` when finished.

### Connecting from another machine

The CLI protocol is a plain socket: the shared-secret challenge authenticates
the connection but does **not** encrypt credentials or file contents. Keep the
ports bound to localhost and use an encrypted tunnel, for example:

```bash
ssh -N -L 9640:127.0.0.1:9640 -L 7640:127.0.0.1:7640 user@container-host
```

Run the CLI on the remote machine as above, with `ecpds-mover` resolving to
its own localhost. Keep the tunnel open during the upload. If the container
already exists without these port mappings, recreate it with the same `/data`
volume and image build; Docker cannot add mappings to an existing container.

For direct access instead, replace the localhost mappings with
`-p HOST_IP:9640:9640` and `-p HOST_IP:7640:7640` when creating the container,
and make `ecpds-mover` resolve to `HOST_IP` on the CLI machine. Use an
encrypted, access-controlled network such as a VPN; do not expose the
plain-socket ports to an untrusted network.

## Related

- [ECPDS Command-line Tool](../use-cases/ecpds-cli.md) — workflow details and options
- [Standalone Container](standalone.md) — running a local OpenECPDS instance
- [Releasing to a Registry](../deployment/release.md) — publishing CLI images
