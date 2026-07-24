# Host Types

OpenECPDS defines six host types. Each type has a distinct role in the data lifecycle and
a different relationship with [Destinations](entities.md#destinations-and-aliases).

## Overview

| Type | Role | Linked to Destination? | Direction |
|------|------|------------------------|-----------|
| [Dissemination](#dissemination) | Push files to external recipients | Yes — many-to-many | Outbound |
| [Acquisition](#acquisition) | Pull files from remote sources | Yes — many-to-many | Inbound |
| [Proxy](#proxy) | Pre-replicate to a Continental Data Mover | Yes — many-to-many | Outbound pre-staging |
| [Source](#source) | Re-fetch a file that is no longer in local storage | Yes — via Destination field | Recovery |
| [Backup](#backup) | Secondary storage within a Transfer Group | Via Transfer Group only | Internal |
| [Replication](#replication) | Replicate data between internal Data Movers | No | Internal |

---

## Dissemination

A **Dissemination** host pushes data files to an external recipient system. It is the
primary mechanism for delivering data from the OpenECPDS Data Store to a remote site.

### Relationship with Destinations

One or more Dissemination hosts are associated with a Destination through the
**Association** table. A single Dissemination host can serve multiple Destinations with
independently configurable priorities. When a transfer is dispatched, hosts are evaluated
in ascending priority order (lower number = higher precedence). If the highest-priority
host fails, the system automatically falls back to the next one.

This many-to-many relationship makes it possible to share a common host definition
(e.g. an institutional FTP server) across many Destinations without duplication.

### Key configuration

- **Transfer Method** — the protocol module used for the connection (FTP, SFTP, FTPS,
  HTTP/S, S3, Azure, GCS, ECauth …). See [Transfer Modules](../transfer-modules/index.md).
- **Login / Password / Key** — credentials for authenticating to the remote server.
- **Directory** — target-path template applied to every transfer, supporting variable
  substitution and selector syntax. See [Dissemination Directory](../host-directory/dissemination.md).
- **Priority** — evaluated per Association; controls failover order within a Destination.
- **Properties** — fine-tuned options such as Data Mover allocation rules and upload
  settings. See [Host Options](host-options.md).

### When to use

Use a Dissemination host whenever you need to deliver files to an external system as part
of a scheduled or triggered dissemination workflow. This is the most common host type.

---

## Acquisition

An **Acquisition** host pulls files from a remote source on behalf of a Destination. It
drives the inbound data pipeline: OpenECPDS periodically connects to the remote system,
discovers new files according to the configured listing rules, and retrieves them into
the Data Store.

### Relationship with Destinations

Like Dissemination hosts, Acquisition hosts are linked to Destinations via the
**Association** table (many-to-many with priority). If a Destination has both Acquisition
and Dissemination hosts, OpenECPDS can automatically retrieve data from one place and
disseminate it to another — a popular ingest-and-forward pattern.

Multiple Acquisition hosts on the same Destination are contacted **simultaneously** (not
sequentially); priority has no effect on acquisition scheduling but is shown for
reference.

### Key configuration

- **Transfer Method** — protocol used to connect to the remote source.
- **Login / Password / Key** — credentials for the remote source.
- **Directory** — listing specification: one or more remote directories to scan, with
  rich filtering rules for file name, date, size, and more. JavaScript and Python scripts
  are supported for dynamic listing logic. See
  [Acquisition Directory](../host-directory/acquisition.md).
- **Properties** — options for Data Mover selection and virtual FTP home directory. See
  [Host Options](host-options.md) and [Acquisition Options](../use-cases/acquisition-options.md).

### When to use

Use an Acquisition host whenever OpenECPDS needs to pull files from a remote site
rather than receive a push. See also [Acquisition](../use-cases/acquisition.md).

---

## Proxy

A **Proxy** host associates a **Continental Data Mover** with a Destination for
pre-replication. Before the scheduled dissemination time, OpenECPDS proactively replicates
data from an internal Data Mover to the Continental Data Mover. When the schedule arrives,
the Continental Data Mover delivers directly to the target, reducing latency for
geographically distributed users. If the Continental Data Mover is unavailable, the
system falls back to a standard internal Data Mover.

### Relationship with Destinations

Proxy hosts use the same **Association** table as Dissemination and Acquisition hosts
(many-to-many with priority). A single Proxy host can be shared across multiple
Destinations.

### Key configuration

- **Transfer Method** — the protocol used by the Continental Data Mover to deliver files
  to the target.
- **Directory** — base path on the Continental Data Mover. See
  [Replication, Source, Backup & Proxy Directory](../host-directory/replication.md).
- **Properties** — `proxy.*` options control the HTTP URL, retry modulo, timeout, and
  filter reuse. See [Host Options](host-options.md).

### When to use

Use a Proxy host when you have a Continental Data Mover deployed near your users' region
and want to pre-stage data there before dissemination. See
[Continental Data Movers](../architecture/continental-data-movers.md) for a full
architectural description.

---

## Source

A **Source** host provides a **recovery retrieval path**. If a file that needs to be
re-sent is no longer available in local storage (e.g. it has been cleaned up), OpenECPDS
contacts the Source host to re-fetch it before retrying the transfer.

### Relationship with Destinations

A Source host is referenced directly in the **Destination** record through the Destination's
*Source Host* field — not through the Association table. A given Source host can
therefore be referenced by one or more Destinations (multiple Destinations can point to
the same Source host), but each Destination holds only one Source host reference at a time.

### Key configuration

- **Transfer Method** — protocol used to retrieve the file from the source system.
- **Login / Password / Key** — credentials for the source system.
- **Directory** — base path on the source system. See
  [Replication, Source, Backup & Proxy Directory](../host-directory/replication.md).

### When to use

Use a Source host when your Destination must be able to recover files from an upstream
archive or staging area in the event that the local copy has been purged from the
OpenECPDS Data Store.

---

## Backup

A **Backup** host provides secondary storage within a **Transfer Group**. When the
primary Data Movers in the Transfer Group are unavailable or at capacity, data can be
written to the Backup host instead, ensuring continuity of service.

### Relationship with Destinations

Backup hosts are **not** linked to Destinations directly. They are referenced by a
Transfer Group via the Transfer Group's *Backup Host* field. A Destination is associated
with a Transfer Group; the Backup host relationship is therefore indirect, at the
infrastructure level rather than the dissemination level.

### Key configuration

- **Transfer Method** — protocol used to connect to the backup storage system.
- **Login / Password / Key** — credentials for the backup system.
- **Directory** — base path on the backup system. See
  [Replication, Source, Backup & Proxy Directory](../host-directory/replication.md).

### When to use

Use a Backup host to add resilience to a Transfer Group when a dedicated fallback storage
target is available and you want automated failover within the group.

---

## Replication

A **Replication** host is used internally to replicate data files between
**OpenECPDS Data Movers**. It operates at the infrastructure level and is managed by the
platform — not associated with any Destination or Transfer Group.

### Relationship with Destinations

Replication hosts are **not linked to any Destination**. They are purely internal and
managed at the Data Mover / platform level.

### Key configuration

- **Transfer Method** — the internal protocol used for Data Mover-to-Data Mover
  replication (typically ECauth or a dedicated module).
- **Directory** — base path on the target Data Mover. See
  [Replication, Source, Backup & Proxy Directory](../host-directory/replication.md).

### When to use

Replication hosts are created and managed by platform administrators when setting up
multi-mover environments. They are not typically created or edited by end users.

---

## Choosing the right host type

| Scenario | Recommended type |
|----------|-----------------|
| Deliver files to an external FTP/SFTP/S3/Azure/GCS site | **Dissemination** |
| Pull files from a remote server into the Data Store | **Acquisition** |
| Speed up delivery to geographically distant users | **Proxy** (with a Continental Data Mover) |
| Re-fetch lost files from an upstream archive | **Source** |
| Provide fallback storage within a Transfer Group | **Backup** |
| Replicate data between internal Data Movers | **Replication** |

---

## Related

- [OpenECPDS Entities](entities.md) — foundational concepts (Destinations, data files, data transfers)
- [Failover Mechanism](../architecture/failover.md) — how Dissemination host failover works
- [Continental Data Movers](../architecture/continental-data-movers.md) — Proxy host architecture
- [Host Directory Field](../host-directory/index.md) — how the Directory field is interpreted per type
- [Host Options](host-options.md) — Properties field reference
- [Transfer Modules](../transfer-modules/index.md) — available protocol implementations
- [Acquisition](../use-cases/acquisition.md) — acquisition use-case walkthrough
- [Dissemination](../use-cases/dissemination.md) — dissemination use-case walkthrough
