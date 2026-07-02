# Object Storage

OpenECPDS stores data as **objects**, combining data, metadata, and a globally unique
identifier. It employs a file-system-based solution with replication across multiple
locations to ensure continuous data availability. For example, data can be replicated
across local storage systems and cloud platforms to bring data closer to users and
enhance performance.

## Hierarchy-free with emulated structure

The object storage system in OpenECPDS is **hierarchy-free** but can **emulate directory
structures** when necessary, based on metadata provided by data providers. OpenECPDS
presents different views of the same data, depending on user preferences.

When the [Data Portal](../use-cases/data-portal.md) is used to pull data, a destination
with no dissemination hosts can be seen as:

- A **bucket** (in Amazon S3 terms), or
- A **blob container** (in Microsoft Azure terms).

See [OpenECPDS Entities](entities.md#destinations-and-aliases) for how destinations relate
to this model.

## Cloud object storage backends

OpenECPDS supports major cloud object storage platforms both for outgoing transfers
(dissemination/acquisition) and as storage backends:

| Backend | Module |
|---------|--------|
| Amazon S3 | [Amazon S3 Transfer Module](../transfer-modules/s3.md) |
| Google Cloud Storage | [Google Cloud Storage Transfer Module](../transfer-modules/gcs.md) |
| Azure Blob Storage | [Azure Blob Storage Transfer Module](../transfer-modules/azure.md) |

## Data integrity & lifecycle

- **Data Checksumming** — MD5 for data integrity checks on remote sites, and ADLER32 for
  data integrity checks in the Data Store.
- **Data Compression** — supports lzma, zip, gzip, bzip2, lbzip2, lz4 and snappy.
- **Garbage Collection** — automatically removes expired data, with no limit on expiry
  dates.
- **Data Backup** — can be configured to map data sets in OpenECPDS to existing archiving
  systems.

See [Additional Features](additional-features.md) for more on these capabilities.

## Related

- [Protocols & Connections](protocols.md)
- [Additional Features](additional-features.md)
- [Transfer Modules](../transfer-modules/index.md)
