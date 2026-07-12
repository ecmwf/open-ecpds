# Additional Features

Beyond its core acquisition, dissemination, and portal services, OpenECPDS provides a
number of additional platform features.

## Notification System

Provides an embedded **MQTT broker** to publish notifications and an **MQTT client** to
subscribe to data providers. This enables real-time data distribution and automated
acquisition. See the [MQTT Notification System](../notifications/mqtt-overview.md).

## Data Compression

Supports various algorithms to reduce dissemination time and enable faster access to
data:

- lzma
- zip
- gzip
- bzip2
- lbzip2
- lz4
- snappy
- zstd

## Data Checksumming

- **MD5** — for data integrity checks on the remote sites.
- **ADLER32** — for data integrity checks in the Data Store.

## Garbage Collection

Automatically removes expired data, with **no limit on expiry dates**.

## Data Backup

Can be configured to map data sets in OpenECPDS to existing archiving systems.

## Related

- [Object Storage](object-storage.md)
- [MQTT Notification System](../notifications/mqtt-overview.md)
- [Protocols & Connections](protocols.md)
