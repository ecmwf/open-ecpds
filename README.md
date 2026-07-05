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

## 📖 Documentation

Full documentation is published at **[ecmwf.github.io/open-ecpds](https://ecmwf.github.io/open-ecpds/)**.

| Section | Description |
|---|---|
| [Getting Started](https://ecmwf.github.io/open-ecpds/getting-started/requirements/) | System requirements, installation, first run, IDE setup |
| [Architecture](https://ecmwf.github.io/open-ecpds/architecture/overview/) | Components, failover, data transfer lifecycle, continental data movers |
| [Deployment](https://ecmwf.github.io/open-ecpds/deployment/kubernetes/) | Kubernetes, physical infrastructure, container registry |
| [Concepts](https://ecmwf.github.io/open-ecpds/concepts/entities/) | Entities, protocols, object storage, destination/host/web-user options |
| [Use Cases](https://ecmwf.github.io/open-ecpds/use-cases/ecpds-cli/) | CLI tool, data portal, acquisition, dissemination |
| [Transfer Modules](https://ecmwf.github.io/open-ecpds/transfer-modules/) | FTP, SFTP, HTTP/S, S3, GCS, Azure, ECauth, Portal, Test |
| [Host Directory](https://ecmwf.github.io/open-ecpds/host-directory/) | Acquisition, dissemination, replication, source, backup, proxy |
| [Notifications (MQTT)](https://ecmwf.github.io/open-ecpds/notifications/mqtt-overview/) | Real-time dissemination, automated acquisition, WMO WIS2 |
| [Event Logging](https://ecmwf.github.io/open-ecpds/event-logging/overview/) | PRS, RET, UPH, INH, ERR, CPY, DEA event categories |
| [API Reference](https://ecmwf.github.io/open-ecpds/api-reference/) | JavaDocs |
| [Changelog](https://ecmwf.github.io/open-ecpds/changelog/) | Release notes |
| [Contributing](https://ecmwf.github.io/open-ecpds/contributing/) | How to contribute |

---

## 🚀 Quick Start

Requires **Docker** (with the default socket enabled). Tested on Linux, macOS (Intel/Apple Silicon), and Windows WSL 2.

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

The monitoring UI is then available at `https://localhost:4443` and the data portal at `https://localhost:4443/ecpds`.

See [Getting Started](https://ecmwf.github.io/open-ecpds/getting-started/requirements/) for the full walkthrough, including log inspection, stopping services, IDE setup, and Kubernetes deployment.

---

## 📜 License

Copyright 2022–2024 ECMWF. Licensed under the [Apache License 2.0](LICENSE.txt).
