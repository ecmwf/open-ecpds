# Transfer Modules

OpenECPDS is built around a **modular transfer system** (internally referred to as
*ECtrans modules*). Each module implements a specific network protocol or storage
backend, and is selected for a transfer based on the [Transfer Method](../concepts/entities.md#transfer-methods)
configured on a [Host](../concepts/entities.md#dissemination-and-acquisition-hosts).
Because the system is modular, new protocols can be added through extensions without
changing the core of OpenECPDS.

A module is responsible for the low-level operations of a transfer:

- **connect** — establish a connection (or authenticate) to the remote system.
- **put / get** — upload or download file content.
- **size** — verify the size of a transferred object.
- **del** — delete a remote object (where supported).
- **copy** — server-side or simulated copy (where supported).

## Supported protocols & backends

| Module | Protocol / backend | Direction | Page |
|--------|--------------------|-----------|------|
| FTP | File Transfer Protocol | Dissemination & Acquisition (also incoming Data Portal) | [FTP](ftp.md) |
| FTPS | FTP over TLS/SSL | Dissemination & Acquisition | [FTPS](ftps.md) |
| SFTP | SSH File Transfer Protocol | Dissemination & Acquisition | [SFTP](sftp.md) |
| HTTP / HTTPS | HyperText Transfer Protocol (with embedded MQTT client) | Dissemination & Acquisition | [HTTP/HTTPS](http.md) |
| S3 | Amazon S3 object storage | Dissemination & Acquisition | [Amazon S3](s3.md) |
| GCS | Google Cloud Storage | Dissemination & Acquisition | [Google Cloud Storage](gcs.md) |
| Azure | Azure Blob Storage | Dissemination & Acquisition | [Azure Blob Storage](azure.md) |
| ECauth | SSH/Telnet authentication transport | Dissemination & Acquisition | [ECauth](ecauth.md) |
| Portal | No-op local staging for Data Portal pull | Dissemination | [Portal](portal.md) |
| Test | Simulation / benchmarking | Dissemination | [Test](test.md) |

For a higher-level discussion of incoming and outgoing protocols, see
[Protocols & Connections](../concepts/protocols.md).

## Option syntax

Each module exposes a set of configuration options. Options are written as
`key=value` pairs, one per line, in the host's option editor. Every option is
namespaced with the module's prefix — for example `ftp.port`, `sftp.cipher`,
`s3.bucketName`, `http.*`, `test.bytesPerSec`. Options that are not relevant to the
selected module are ignored.

In addition to module-specific options, a set of [ECtrans common options](ectrans.md)
(`ectrans.*`) applies to every module and controls shared behaviour such as connection
retries, timeouts, stream buffering, compression, external handlers, notifications,
and TCP-level tuning.

```properties
# Example: a few SFTP options
sftp.port = "22"
sftp.cipher = "aes128-ctr"
```

Values commonly accept human-friendly notation:

- **Byte sizes** — e.g. `10MB`, `1GB`.
- **Durations** — e.g. `500ms`, `1s`, `5min`.
- **Booleans** — `yes`/`no` or `true`/`false` (see each module page for specifics).

## Where modules fit

- The [Mover Server (Data Mover)](../architecture/components.md#mover-server-data-mover)
  loads the appropriate module at transfer time.
- The [Directory field](../host-directory/index.md) of a host controls *where* the
  file is read from or written to on the remote system, independently of the module.
- The [data-transfer lifecycle](../architecture/data-transfer-lifecycle.md) describes
  how transfers move through their statuses, including failures and retries handled at
  the module level.

## Related

- [ECtrans Common Options](ectrans.md)
- [OpenECPDS Entities](../concepts/entities.md)
- [Protocols & Connections](../concepts/protocols.md)
- [Object Storage](../concepts/object-storage.md)
- [Host Directory Field](../host-directory/index.md)
