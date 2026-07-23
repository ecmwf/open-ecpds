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

Built on container technologies, it scales from a laptop to hundreds of systems managing petabytes of data. It supports a wide range of protocols (FTP, SFTP, HTTP/S, S3, GCS, Azure Blob, MQTT/WIS2, …) and integrates with object storage, MQTT brokers, and Kubernetes.

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
aws s3 ls s3:// \
  --endpoint-url https://localhost:7443/s3 \
  --no-verify-ssl \
  --no-sign-request \
  --request-payer \
  # or with credentials:
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

**SFTP** — using the `sftp` command-line client:
```bash
sftp -P 7022 -o StrictHostKeyChecking=no test@localhost
# Password: test2021
```


**MQTTS** — using [Mosquitto](https://mosquitto.org/download/) client:
```bash
mosquitto_sub \
  --host localhost --port 8883 \
  --username test --pw test2021 \
  --insecure \
  --topic '#' -v
```

### Logs

```bash
# All services (supervisord output)
docker logs -f standalone

# Individual service logs
docker exec standalone tail -f /data/log/master/master.output
docker exec standalone tail -f /data/log/mover/mover.output
docker exec standalone tail -f /data/log/monitor/monitor.output
```

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
| [Transfer Modules](https://ecmwf.github.io/open-ecpds/transfer-modules/) | FTP, FTPS, SFTP, HTTP/S, S3, GCS, Azure Blob, ECauth, Portal, Test |
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
# Inside the development container, after 'make build':
make build-standalone
```

This stages the RPMs into `docker/ecpds/standalone/` and builds the `ecpds/standalone:<tag>` image locally.

---

## 📜 License

Copyright 2022–2024 ECMWF. Licensed under the [Apache License 2.0](LICENSE.txt).
