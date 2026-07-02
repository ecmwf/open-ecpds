# Protocols & Connections

OpenECPDS interacts with a variety of environments and supports multiple standard
protocols. The software is modular, supporting new protocols through extensions (see
[Transfer Modules](../transfer-modules/index.md)).

## Outgoing connections

Used by the **Data Acquisition** and **Data Dissemination** services:

- FTP
- SFTP
- FTPS
- HTTP / HTTPS
- Amazon S3
- Azure Blob Storage
- Google Cloud Storage

## Incoming connections

Used by the **Data Portal**:

- FTP
- HTTPS
- S3

!!! note
    At the moment, **SFTP** and **SCP** for incoming Data Portal connections are
    available exclusively through a **Commercial API**.

## Authentication & connection methods

Protocol configurations vary based on authentication and connection methods, for example:

- **Password** vs. **key-based** authentication.
- **Parallel** vs. **serial** connections.

Each [transfer module](../transfer-modules/index.md) exposes its own set of options to
fine-tune these aspects. See the individual module pages:

| Protocol | Module page |
|----------|-------------|
| FTP | [FTP](../transfer-modules/ftp.md) |
| FTPS | [FTPS](../transfer-modules/ftps.md) |
| SFTP | [SFTP](../transfer-modules/sftp.md) |
| HTTP/HTTPS | [HTTP/HTTPS](../transfer-modules/http.md) |
| Amazon S3 | [Amazon S3](../transfer-modules/s3.md) |
| Google Cloud Storage | [GCS](../transfer-modules/gcs.md) |
| Azure Blob Storage | [Azure](../transfer-modules/azure.md) |
| SSH/Telnet auth | [ECauth](../transfer-modules/ecauth.md) |

## Related

- [OpenECPDS Entities](entities.md) — Transfer Methods and Hosts
- [Object Storage](object-storage.md)
- [Transfer Modules](../transfer-modules/index.md)
