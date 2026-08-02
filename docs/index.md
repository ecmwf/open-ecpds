# OpenECPDS Documentation

![OpenECPDS](img/OpenECPDS.svg){ width="420" .no-box }

> **Our mission with OpenECPDS is to keep data moving.**
>
> Inspired by operational excellence. Powered by open-source innovation. Acquire from
> anywhere. Deliver everywhere. Connect with confidence. Share without limits.

OpenECPDS is a multi-purpose data repository — the **Data Store** — that delivers three
strategic data-related services:

- **Data Acquisition** — the automatic discovery and retrieval of data from data providers.
- **Data Dissemination** — the automatic distribution of data products to remote sites.
- **Data Portal** — the pulling and pushing of data initiated by remote sites.

Data Acquisition and Data Dissemination are active services initiated by OpenECPDS,
whereas the Data Portal is a passive service triggered by incoming requests from remote
sites. The Data Portal provides interactive access to the Dissemination and Acquisition
services.

OpenECPDS enhances data services by integrating innovative technologies to streamline
the acquisition, dissemination, and storage of data across diverse environments and
protocols.

## Why OpenECPDS

<div class="grid cards" markdown>

- :material-cloud-sync: **Acquire from anywhere**

    Automatically discover and retrieve data from providers over FTP, SFTP, FTPS,
    HTTP/S, WebDAV, Amazon S3, Azure and Google Cloud Storage.

- :material-share-variant: **Deliver everywhere**

    Disseminate products to more than 1,000 destinations across 80+ countries with a
    fully customisable, retry-aware transfer scheduler.

- :material-web: **Data Portal for all users**

    A built-in portal lets anonymous, self-registered, or authorised users push and
    pull data over FTP, SFTP, SCP, HTTPS, WebDAV and S3 — no custom integration needed.
    One platform handles both incoming and outgoing traffic.

- :material-bell-ring: **Real-time notifications**

    An embedded MQTT broker and client enable instant notifications and integration
    with the WMO WIS2 infrastructure.

- :material-docker: **Container-native**

    The [standalone image](getting-started/standalone.md) runs the full stack on a
    laptop for instant evaluation. For production, each component (Master, Data Mover,
    Monitor) has its own container and can be deployed independently or orchestrated at
    scale with [Kubernetes](deployment/kubernetes.md).

- :material-source-branch: **Open and extensible**

    New outgoing protocols plug into the [transfer module framework](transfer-modules/index.md);
    new incoming protocols plug into the [server plugin framework](concepts/plugin-infrastructure.md)
    (`SshPlugin`, `HttpPlugin`, `FtpPlugin`…). Open-source and actively developed by ECMWF.

</div>

[Why OpenECPDS? →](why-openecpds.md){ .md-button }

## Quick links

- **New here?** Try the [Standalone container](getting-started/standalone.md) first — no build needed.
  For the full setup: [System Requirements](getting-started/requirements.md) →
  [Installation](getting-started/installation.md) → [First Run](getting-started/first-run.md).
- **Understand the system:** [Architecture Overview](architecture/overview.md) and
  [Key Concepts](concepts/entities.md).
- **Configure transfers:** [Transfer Modules](transfer-modules/index.md) and the
  [Host Directory Field](host-directory/index.md).
- **Operate & monitor:** [Event Logging](event-logging/overview.md) and the
  [MQTT Notification System](notifications/mqtt-overview.md).

## Architecture at a glance

![ECMWF Product Data Store (ECPDS)](img/Figure01.svg){ width="450" }

| Component | Role |
|-----------|------|
| [Master Server](architecture/components.md#master-server) | Central coordinator — authentication, metadata, scheduling, Data Mover allocation. |
| [Mover Server (Data Mover)](architecture/components.md#mover-server-data-mover) | Moves bytes — connects to remote systems via transfer modules, stores/streams content. |
| [Monitor Server](architecture/components.md#monitor-server) | Web monitoring interface for destinations, transfers and hosts. |
| [Data Portal](architecture/components.md#data-portal) | Incoming FTP/HTTPS/WebDAV/S3 access for remote sites to push and pull data. |
| Database | Persists metadata, destinations, hosts, transfers and history. |

See the [Architecture Overview](architecture/overview.md) for how these components work
together, and [Continental Data Movers](architecture/continental-data-movers.md) for
geographically distributed dissemination.

## Core capabilities

- **Multiple protocols** — FTP, SFTP, FTPS, SCP, HTTP/S, WebDAV, Amazon S3, Azure Blob and Google
  Cloud Storage. See [Protocols & Connections](concepts/protocols.md).
- **Data Portal — incoming and outgoing in one platform** — remote users can push
  data in *and* pull data out over FTP, SFTP, SCP, HTTPS, WebDAV and S3. Access can be
  restricted to authorised users, opened anonymously, or offered as self-service
  registration — all without any custom integration. Dissemination and Acquisition data
  is accessible through the same portal, making OpenECPDS a complete two-way data
  exchange hub. See [Data Portal](use-cases/data-portal.md).
- **Object storage** — hierarchy-free storage that can emulate directory structures.
  See [Object Storage](concepts/object-storage.md).
- **Notification system** — embedded MQTT broker and client. See
  [MQTT Overview](notifications/mqtt-overview.md).
- **Data compression** — lzma, zip, gzip, bzip2, lbzip2, lz4, snappy, zstd.
- **Data checksumming** — MD5 for remote integrity, ADLER32 in the Data Store.
- **Garbage collection** — automatic removal of expired data.
- **Extensible plugin architecture** — new outgoing protocols are added via the
  [transfer module framework](transfer-modules/index.md); new incoming (Data Portal)
  protocols via the [server plugin framework](concepts/plugin-infrastructure.md)
  (`SshPlugin`, `HttpPlugin`, `FtpPlugin`, `MqttPlugin`…). Both sides can be extended
  without modifying the core platform.
- **Data backup** — map data sets to existing archiving systems.

See [Additional Features](concepts/additional-features.md) for a full breakdown of platform capabilities.

## Support & resources

- [Javadoc API documentation](https://ecmwf.github.io/open-ecpds/javadoc/)
- [Support Materials](support.md)
- [Glossary](glossary.md) of key terms
- [Contributing](contributing.md) and [Changelog](changelog.md)
