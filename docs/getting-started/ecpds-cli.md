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

Use it as a one-shot command (mount the files you want to submit):

```bash
docker run --rm \
  -v /path/to/data:/data \
  ghcr.io/ecmwf/open-ecpds/cli:latest \
  -echost master.example.com -ecport 9640 \
  -user mylogin -pass mypassword \
  -destination MY_DESTINATION \
  -source /data/myfile.dat
```

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

If you are running the [standalone container](standalone.md), the Master Server listens
on port **9640** for `ecpds` CLI connections. The default credentials are
`test` / `test2021` and a destination named `TEST` is pre-configured.

```bash
ecpds \
  -echost localhost -ecport 9640 \
  -user test -pass test2021 \
  -destination TEST \
  -source /path/to/myfile.dat
```

Or using the CLI Docker image:

```bash
docker run --rm \
  -v /path/to/myfile.dat:/myfile.dat \
  ghcr.io/ecmwf/open-ecpds/cli:latest \
  -echost host.docker.internal -ecport 9640 \
  -user test -pass test2021 \
  -destination TEST \
  -source /myfile.dat
```

!!! note
    On Linux, use `--network host` instead of `host.docker.internal` to reach services
    on the host machine from within a Docker container.

Or directly inside the container (no network configuration needed):

```bash
docker exec standalone ecpds \
  -echost localhost -ecport 9640 \
  -user test -pass test2021 \
  -destination TEST \
  -source /path/to/myfile.dat
```

## Related

- [ECPDS Command-line Tool](../use-cases/ecpds-cli.md) — workflow details and options
- [Standalone Container](standalone.md) — running a local OpenECPDS instance
- [Releasing to a Registry](../deployment/release.md) — publishing CLI images
