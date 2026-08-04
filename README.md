<img src="img/OpenECPDS.svg" alt="OpenECPDS" width="500">

> **Our mission with OpenECPDS is to keep data moving.**  
> Inspired by operational excellence. Powered by open-source innovation.  
> Acquire from anywhere. Deliver everywhere. Connect with confidence. Share without limits.

---

OpenECPDS is a mature, production-grade data management platform originally developed at [ECMWF](https://www.ecmwf.int/) and now open-sourced to encourage collaboration. It delivers three strategic services from a single system:

| Service | Description |
|---|---|
| **Data Acquisition** | Automatic discovery and retrieval of data from remote providers |
| **Data Dissemination** | Automatic distribution of data products to remote sites |
| **Data Portal** | Pull/push access initiated by remote sites, with real-time monitoring |

Built on container technologies, it scales from a laptop to hundreds of systems managing petabytes of data. It supports a wide range of protocols (FTP, SFTP, HTTP/S, WebDAV, S3, GCS, Azure Blob, MQTT/WIS2, …) and integrates with object storage, MQTT brokers, and Kubernetes.

---

## 🚀 Try It Now — No Build Required

Experience the full OpenECPDS stack in a single command. The standalone image bundles MariaDB, Master, Data Mover, and Monitor — everything pre-configured and ready to go.

> **Requirements:** 4 GB RAM and 6 GB free disk recommended. See [System Requirements](https://ecmwf.github.io/open-ecpds/getting-started/requirements/) for details.

```bash
docker run -d \
  --name standalone \
  -v $(pwd)/ecpds-data:/data \
  -p 7443:7443 \
  -p 7022:7022 \
  -p 8443:8443 \
  -p 8883:8883 \
  ghcr.io/ecmwf/open-ecpds/standalone:latest
```

The database initialises automatically on first start. The `/data` volume persists everything across restarts.

> **Note:** The container uses a self-signed TLS certificate — accept the browser security warning on first visit.

### Access

| Service | URL | Credentials |
|---|---|---|
| Monitoring UI | `https://localhost:8443` | admin / admin2021 · monitor / monitor2021 |
| Data Portal (HTTPS) | `https://localhost:7443` | test / test2021 |
| Data Portal (S3) | `https://localhost:7443/s3` | test / test2021 |
| Data Portal (WebDAV) | `https://localhost:7443/webdav` | test / test2021 |
| Data Portal (SFTP) | `sftp://localhost:7022` | test / test2021 |
| MQTTS broker | `mqtts://localhost:8883` | test / test2021 |

### Exposed ports

| Port | Service |
|---|---|
| `7443` | Data Mover — HTTPS portal |
| `7022` | Data Mover — SFTP |
| `8883` | Data Mover — MQTTS (MQTT over TLS) |
| `8443` | Monitor — HTTPS UI |
| `9640` | Master — ECpds CLI |

> **Note on FTP:** OpenECPDS fully supports FTP in production deployments. However, FTP passive mode (PASV) is not compatible with Docker port mapping — the server advertises its internal container address for data connections, which external clients cannot reach. For that reason, FTP is disabled in this standalone image. Use SFTP (port 7022) as a drop-in alternative for file transfers in Docker.

### Testing the protocols

All examples below use the pre-configured `test / test2021` account. The container uses a self-signed certificate — pass the appropriate insecure/skip-verify flag for each tool.

**S3** — using [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html):
```bash
aws configure set aws_access_key_id test
aws configure set aws_secret_access_key test2021
aws s3 ls s3:// --endpoint-url https://localhost:7443/s3 --no-verify-ssl
```

Or with [rclone](https://rclone.org/):
```bash
rclone lsd :s3: \
  --s3-provider=Other \
  --s3-endpoint=https://localhost:7443/s3 \
  --s3-access-key-id=test \
  --s3-secret-access-key=test2021 \
  --no-check-certificate
```

**WebDAV** — using [curl](https://curl.se/):
```bash
# List root directory (PROPFIND)
curl -k -u test:test2021 -X PROPFIND https://localhost:7443/webdav/
```

Or with [rclone](https://rclone.org/):
```bash
rclone lsd :webdav: \
  --webdav-url=https://localhost:7443/webdav \
  --webdav-vendor=other \
  --webdav-user=test \
  --webdav-pass=$(rclone obscure test2021) \
  --no-check-certificate
```

Or mount directly from your OS file manager:

- **macOS Finder:** Go → **Connect to Server…** (⌘K), enter `https://localhost:7443/webdav`, click **Connect**, then log in with `test` / `test2021`.
- **Windows Explorer:** Right-click **This PC → Map network drive**, choose a drive letter, enter `https://localhost:7443/webdav` as the folder, check **Connect using different credentials** and log in with `test` / `test2021`.
- **Linux (GNOME Files / Nautilus):** Go → **Connect to Server…**, enter `davs://localhost:7443/webdav`, log in with `test` / `test2021`.
- **Linux (command-line):** mount with `davfs2`: `sudo mount -t davfs https://localhost:7443/webdav /mnt/webdav`

> **Note:** All OS file managers will warn about the self-signed certificate — accept it to proceed.
> The WebDAV share appears as a network drive alongside your local disks.

**SFTP** — using the `sftp` command-line client:
```bash
sftp -P 7022 -o StrictHostKeyChecking=no test@localhost
# Password: test2021
```


**MQTTS** — The broker uses TLS with a self-signed certificate.

Using [mqttx](https://mqttx.app/cli) CLI (recommended — supports true `--insecure`):
```bash
mqttx sub \
  -h localhost -p 8883 \
  -l mqtts \
  -u test -P test2021 \
  --insecure \
  -t '#'
```

### Submitting files with the ecpds CLI

The `ecpds` command-line client submits files directly to the Master Server (port **9640**).
It is included in the standalone image — no separate installation needed:

```bash
docker exec standalone ecpds \
  -host localhost -port 9640 \
  -user test -pass test2021 \
  -dest TEST \
  /path/to/myfile.dat
```

Alternatively, pull the dedicated CLI image and point it at the container:

```bash
# Pull the CLI image
docker pull ghcr.io/ecmwf/open-ecpds/cli:latest

# Submit a file (Linux — use --network host to reach the standalone container)
docker run --rm --network host \
  -v /path/to/myfile.dat:/myfile.dat \
  ghcr.io/ecmwf/open-ecpds/cli:latest \
  -host localhost -port 9640 \
  -user test -pass test2021 \
  -dest TEST \
  /myfile.dat
```

Or download a pre-compiled binary from the
[GitHub Releases page](https://github.com/ecmwf/open-ecpds/releases) (`ecpds-amd64` /
`ecpds-arm64`) and run it directly — the binary only requires OpenSSL:

```bash
curl -Lo /usr/local/bin/ecpds \
  https://github.com/ecmwf/open-ecpds/releases/latest/download/ecpds-amd64
chmod +x /usr/local/bin/ecpds
ecpds -host localhost -port 9640 -user test -pass test2021 -dest TEST /path/to/myfile.dat
```

See [Getting the ecpds CLI](https://ecmwf.github.io/open-ecpds/getting-started/ecpds-cli/)
for the full list of options and installation methods.

### Logs

```bash
# All services (supervisord output)
docker logs -f standalone

# Individual service logs
docker exec standalone tail -f /data/log/master/master.log
docker exec standalone tail -f /data/log/mover/mover.log
docker exec standalone tail -f /data/log/monitor/monitor.log
```

> **Note:** The standalone image runs with `DEBUG` logging enabled by default. This produces
> verbose output that is useful for exploring how OpenECPDS processes transfers, connects to
> data movers, and handles incoming requests. In a production deployment the log level can be
> controlled via the `LOG_LEVEL` environment variable (e.g. `LOG_LEVEL=warn`).

---

## 📖 Documentation

Full documentation is published at **[ecmwf.github.io/open-ecpds](https://ecmwf.github.io/open-ecpds/)**.

| Section | Description |
|---|---|
| [Getting Started](https://ecmwf.github.io/open-ecpds/getting-started/requirements/) | Requirements, installation, first run, IDE setup |
| [Architecture](https://ecmwf.github.io/open-ecpds/architecture/overview/) | Components, failover, data transfer lifecycle, continental data movers |
| [Deployment](https://ecmwf.github.io/open-ecpds/deployment/kubernetes/) | Kubernetes, physical infrastructure, container registry, releasing |
| [Concepts](https://ecmwf.github.io/open-ecpds/concepts/entities/) | Entities, protocols, object storage, destination/host/web-user options, additional features |
| [Use Cases](https://ecmwf.github.io/open-ecpds/use-cases/ecpds-cli/) | CLI tool, acquisition, dissemination, data portal, data users |
| [Transfer Modules](https://ecmwf.github.io/open-ecpds/transfer-modules/) | FTP, FTPS, SFTP, HTTP/S, WebDAV, S3, GCS, Azure Blob, ECauth, Portal, Test |
| [Host Directory](https://ecmwf.github.io/open-ecpds/host-directory/) | Acquisition, dissemination, replication, source, backup, proxy scripts |
| [Notifications (MQTT)](https://ecmwf.github.io/open-ecpds/notifications/mqtt-overview/) | Real-time dissemination notifications, automated MQTT acquisition, WMO WIS2 |
| [Monitoring](https://ecmwf.github.io/open-ecpds/monitoring/transfer-statistics/) | Transfer network statistics, per-connection TCP socket metrics |
| [Event Logging](https://ecmwf.github.io/open-ecpds/event-logging/overview/) | PRS, RET, UPH, INH, ERR, CPY, DEA event categories and field reference |
| [REST API](https://ecmwf.github.io/open-ecpds/rest-api/) | REST API reference (v1) |
| [API Reference](https://ecmwf.github.io/open-ecpds/api-reference/) | JavaDocs |
| [Global Reach](https://ecmwf.github.io/open-ecpds/global-reach/) | 1,000+ destinations across 80+ countries |
| [Glossary](https://ecmwf.github.io/open-ecpds/glossary/) | Key terms and concepts |
| [Changelog](https://ecmwf.github.io/open-ecpds/changelog/) | Release notes |
| [Contributing](https://ecmwf.github.io/open-ecpds/contributing/) | How to contribute |

---

## 🛠 Going Further — Build from Source

To build the platform from source and develop against it, you need **Docker** (with the default socket enabled). Tested on Linux, macOS (Intel/Apple Silicon), and Windows WSL 2.

> Run `make help` at any time to list all available targets.

### 1 — Outside the development container

```bash
# Build the development container image, start it, and open a shell inside it
make dev
```

### 2 — Inside the development container

```bash
# Compile Java sources, build RPM packages, and create Docker images
make build

# Start all OpenECPDS services
cd run/bin/ecpds && make up
```

The monitoring UI is then available at `https://localhost:3443` and the data portal at `https://localhost:4443`.

See [Getting Started](https://ecmwf.github.io/open-ecpds/getting-started/requirements/) for the full walkthrough, including log inspection, stopping services, IDE setup, and Kubernetes deployment.

### Building the standalone image locally

```bash
# Inside the development container:
make build-sa
```

This stages the RPMs (including the `ecpds` CLI binary) into `docker/ecpds/standalone/` and builds the `ecpds/standalone:<tag>` image locally.

---

## 📜 License

Copyright 2022–2026 ECMWF. Licensed under the [Apache License 2.0](LICENSE.txt).
