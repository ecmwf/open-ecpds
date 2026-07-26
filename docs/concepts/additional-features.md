# Additional Features

OpenECPDS does much more than move files from A to B.
It combines a distributed transfer engine, an object-oriented data store, a user-facing portal,
and an event-driven notification layer into a single operational platform.

This page is a **capabilities overview**.
It focuses on what the platform can do across dissemination, acquisition, and portal workflows,
while the detailed syntax and per-option reference live on the dedicated options pages.

## Data Lifecycle

OpenECPDS manages data as operational assets with lifecycle controls that extend well beyond
simple file delivery.
Files can be compressed in transit, checked for integrity, retained for as long as needed,
automatically expired when they are no longer useful, and mapped into backup or archive
arrangements.
This makes it suitable for environments where long-running feeds, storage efficiency,
and retention policy matter just as much as transport.

Capabilities in this area include:

- transfer-time compression using **lzma**, **zip**, **gzip**, **bzip2**, **lbzip2**,
  **lz4**, **snappy**, and **zstd**
- filename and size-based compression control so optimisation can be selective rather than
  global
- **MD5** generation for remote integrity workflows and **ADLER32** validation inside the
  Data Store
- automatic garbage collection for expired content
- configurable file lifetime at both acquisition and scheduling stages
- backup mapping to existing archival systems

See [Host Options](host-options.md),
[Destination Options](destination-options.md),
and [Object Storage](object-storage.md).

## Notification System

OpenECPDS includes both sides of an event-driven workflow:
an embedded **MQTT broker** for publication and an **MQTT client** for subscription-based
acquisition.
That means the platform can notify downstream consumers as soon as data becomes available,
or react to upstream notifications without polling-heavy integration patterns.
The same foundations also support meteorological distribution patterns such as **WMO WIS2**.

Capabilities in this area include:

- broker-based publication for completed transfers and portal-driven events
- client-side acquisition triggered by MQTT notifications from external providers
- destination-level MQTT publication controls for topic naming, QoS, content type,
  retention, client targeting, and message expiry
- JavaScript-generated MQTT payloads when static text is not enough
- retained-message workflows for late subscribers
- durable MQTT acquisition sessions with persistence and session expiry controls
- WIS2-style extraction of links, timestamps, identifiers, and payload metadata

See [Destination Options](destination-options.md),
[MQTT Overview](../notifications/mqtt-overview.md),
and [Protocols & Connections](protocols.md).

## Transfer Reliability & Automation

Reliable transfer in OpenECPDS is not limited to retry counters.
Hosts can define connect, close, list, get, put, move, delete, and size-operation timeouts,
apply retry logic at the transfer-engine level, and use handlers or protocol hooks before and
after key actions.
This gives operators a practical way to integrate with remote-site quirks,
automate side effects, and protect the platform from stalled or degraded sessions.

Capabilities in this area include:

- per-host retry count and retry frequency controls
- granular operation timeouts across the ECtrans engine
- monitored stream handling and buffering controls for long-running data flows
- pre-transfer and post-transfer handlers for **GET** and **PUT** workflows
- MQTT-based notification callbacks before and after successful transfer completion
- cross-master completion notification so equivalent work can be stopped on another master
- upload and retrieval throttling, minimum-rate thresholds, and maximum-duration checks
- interrupt-on-slow-transfer behaviour for both upload and retrieval paths

See [Host Options](host-options.md),
[Destination Options](destination-options.md),
and [ECtrans Common Options](../transfer-modules/ectrans.md).

## TCP & Network Tuning

OpenECPDS exposes low-level socket controls that are rarely available in general-purpose
transfer tools.
This allows deployments to tune behaviour for specific WAN links, security zones,
high-latency international paths, or cloud-connected environments where the default kernel
behaviour is not always ideal.
In practice, these settings help teams turn network engineering advice into repeatable per-host
transfer policy.

Capabilities in this area include:

- per-host **TCP congestion control** selection, including algorithms such as **BBR** where
  supported by the operating system
- TCP keepalive enablement plus time, interval, and probe-count tuning
- **TCP_NODELAY** control to disable Nagle buffering when low-latency small writes matter
- **SO_LINGER**, user-timeout, quick-ack, window-clamp, timestamp, max-segment, and pacing
  controls
- send and receive buffer sizing for protocols that expose their own data sockets
- socket statistics collection for operational analysis
- local bind-address selection for outbound connections

See [Host Options](host-options.md)
and [ECtrans Common Options](../transfer-modules/ectrans.md).

## Data Acquisition Engine

The acquisition engine is built for discovering, normalising, and retrieving data from
heterogeneous providers rather than assuming a clean, modern listing API.
It can parse traditional directory listings, apply wildcard or regex filters,
normalise server language and timestamp formats, and extract dates from filenames in a
timezone-aware way.
This makes it practical to onboard real-world feeds that expose inconsistent naming,
regional month names, deep directory structures, or event-driven HTTP/MQTT catalogs.

Capabilities in this area include:

- synchronous or parallel listing strategies with queue and thread limits
- regex-based and wildcard-based file selection
- server-type normalisation for UNIX, Windows, VMS, MVS, and other listing formats
- configurable server language, short month names, and server timezone handling
- date extraction from filenames using source slices, patterns, deltas, and custom formats
- age- and size-based selection criteria
- optional symlink-style acquisition records for reference-based workflows
- post-retrieval source cleanup through delete actions
- deduplication by target only, by target name, or by name and time
- requeue-on-update, requeue-on-same-size, and requeue-on-condition behaviour
- interruption control and requeue-on-failure handling
- post-retrieval size-check bypass rules for exceptional cases

See [Host Options](host-options.md),
[Acquisition Options](../use-cases/acquisition-options.md),
and [Protocols & Connections](protocols.md).

## Transfer Scheduling & Queue Control

Each destination can behave like its own transfer policy domain.
OpenECPDS can activate or suppress scheduling during selected time windows,
apply per-file delay or lifetime rules, stamp versions from placeholders,
and hold duplicates using conditional requeue logic.
This is especially valuable when one platform must serve very different destinations:
strict operational feeds, best-effort mirrors, embargoed releases, and ad hoc portal uploads.

Capabilities in this area include:

- scheduler activation windows using explicit time ranges
- immediate scheduling with `asap` or additional delay injection
- per-destination and per-file lifetime control
- placeholder-driven version stamping using date, timestamp, destination, target,
  original name, and file time
- standby and no-retrieval request flags
- transfer-group reassignment at submission time
- queue resets when new arrivals should pre-empt already loaded in-memory work
- cross-master completion notification for paired transfer requests
- duplicate holding through JavaScript expressions comparing timestamps and sizes
- filename-targeted force rules that override lifetime, delay, retrieval,
  priority-related behaviour, and standby state

See [Destination Options](destination-options.md).

## Conditional Aliasing & Target Rewriting

OpenECPDS can branch one incoming data flow into multiple operational outcomes without forcing
upstream systems to submit the same data repeatedly.
Through destination aliasing, files can be copied to secondary destinations conditionally,
with rewritten target paths, adjusted metadata, date-derived naming, and per-alias timing
behaviour.
This is useful for building mirrors, archives, regional feeds, or downstream products from a
single source queue.

Capabilities in this area include:

- conditional alias creation using filename patterns or regex selectors
- ignore rules so scratch files or transient names do not propagate
- alias-specific lifetime, priority, eventing, delay, and immediate scheduling behaviour
- path rewriting with placeholders such as `$name`, `$path`, `$parent`, `$destination`,
  `$alias`, and `$date`
- date extraction and transformation for alias naming,
  including date source selection, formatting, deltas, and parsing rules
- single-line and multi-line rule sets for complex branching policies

See [Destination Options](destination-options.md).

## Distributed Data Mover Allocation

OpenECPDS is designed for distributed transfer infrastructure,
not only for single-node gateways.
Hosts can direct acquisition, dissemination, processing, and backup work to specific
Data Movers based on transfer groups,
and proxy paths can hand work to continental or remote movers when that topology is more
appropriate than direct delivery.
This is a major part of how the platform scales geographically and operationally.

Capabilities in this area include:

- rule-based Data Mover selection for **source**, **processing**, and **backup** roles
- transfer-group-aware routing using equality, inequality, prefix, and suffix matching
- separation between user-facing movers and storage-target movers in portal workflows
- proxy-aware replication towards **Continental Data Movers**
- proxy timeout and retry-modulo controls for hybrid local/remote retry patterns
- reuse of destination compression and filter rules during proxy replication
- virtual FTP home-directory remapping for master-side access patterns

See [Host Options](host-options.md)
and [Continental Data Movers](../architecture/continental-data-movers.md).

## Data Portal — Incoming Controls

The Data Portal is not just a passive upload endpoint.
At destination level,
OpenECPDS can decide how portal-ingested files enter the scheduling system,
how temporary uploads are staged,
how metadata is derived,
and how much bandwidth or quota a destination is allowed to consume.
That lets organisations expose portal access without giving up operational discipline.

Capabilities in this area include:

- destination-level priority, standby, delay, lifetime, and version rules for uploaded files
- event generation when portal uploads should trigger downstream automation
- temporary-file detection based on regex naming patterns,
  with files held until they are renamed to their final name
- metadata injection using placeholders such as date, timestamp, destination, and target
- strict failure handling when metadata parsing must succeed
- custom root-directory naming in the portal presentation layer
- per-destination file sorting and order controls
- transfer-rate caps for both portal uploads and downloads
- rolling-window upload and download quotas enforced per destination

See [Destination Options](destination-options.md)
and [Data Portal](../use-cases/data-portal.md).

## Data Portal — User Features

OpenECPDS can present the same platform differently to different users,
from tightly governed private exchanges to public open-data endpoints.
Per-user portal settings cover connection limits, regional access restrictions,
HTTP delivery behaviour, fine-grained path permissions, MQTT subscription scope,
branding, self-registration, and auditing.
This allows a single deployment to serve operational partners, public subscribers,
and machine-to-machine clients without cloning the platform.

Capabilities in this area include:

- **geo-blocking** by continent, country code, or city
- per-user connection ceilings plus time-of-day schedules that override the default limit
- rolling-window upload and download quotas
- range-request limits for heavy HTTP clients
- custom portal title, tab text, colour, footer, warning banner,
  welcome text, and messages above or below listings
- simple text-only listing mode for `curl`, `wget`, and scripted consumers
- default domain or destination path mapping for cleaner URLs
- per-extension HTTP header rules,
  including controlled MIME-type presentation
- per-user **CORS** configuration for browser-based open-data access
- regex-based permissions for get, put, delete, list, mkdir, rmdir, rename, size,
  and modification-time operations
- MQTT topic permissions for subscriber-facing real-time consumption
- audit-history recording, Splunk forwarding, per-range event suppression,
  and last-login tracking
- self-service registration with either admin approval or automatic activation
- organisation-specific onboarding text in verification and access emails
- access-guide visibility and login-button visibility controls

See [Data User Options](../use-cases/data-portal-user-options.md)
and [MQTT Overview](../notifications/mqtt-overview.md).

## Monitoring & Operator Experience

OpenECPDS separates end-user portal behaviour from monitoring-user behaviour.
That means operators can customise the monitoring interface experience independently,
while still using the same platform to manage destinations, hosts, transfers, and users.
Although the monitoring-side option set is intentionally small,
it reflects the broader goal of making operational workflows configurable per audience.

Capabilities in this area include:

- per-web-user control of the **Share Feedback** action in the monitoring portal
- separate treatment of operational users and data consumers
- alignment between portal-facing user controls and monitor-facing web-user controls

See [Web User Options](web-user-options.md)
and [Data User Options](../use-cases/data-portal-user-options.md).

## Protocol-Specific Optimizations

OpenECPDS combines a common transfer engine with protocol-specific modules,
so operators can tune each integration according to how the remote system actually behaves.
This is important in practice because high-volume FTP,
high-latency SFTP,
HTTP catalog acquisition,
and cloud object transfer each need different optimisation strategies.
The result is a platform that can keep a consistent operational model while still exposing the
specialised controls that real production endpoints require.

### FTP / FTPS

For FTP and FTPS,
OpenECPDS supports connection reuse,
keepalive and NOOP behaviour,
parallel data streams,
temporary-name uploads,
integrity sidecar files,
and raw protocol hooks before or after connect,
GET,
PUT,
and directory-creation steps.
It also exposes port timing,
active/passive behaviour,
extended mode,
and per-socket buffer sizing,
which is useful for legacy or bandwidth-constrained environments.

### SFTP / SSH-style delivery

For SFTP,
OpenECPDS supports private-key authentication,
fingerprint pinning,
preferred authentication ordering,
custom key-exchange and cipher lists,
SSH compression,
and remote post-upload commands.
Performance tuning includes bulk-request read-ahead,
parallel directory listing,
mkdir hooks,
and optional dynamic host allocation and commit callbacks through an external service.

### HTTP / HTTPS and event-driven acquisition

For HTTP,
OpenECPDS can acquire from HTML listings,
CSV/JSON/XML/STAC catalogs,
single-file URLs,
and MQTT-driven discovery feeds.
It supports multipart upload mode selection,
redirect policy,
content compression,
proxying,
TLS controls,
dynamic token refresh through JavaScript,
and parsing rules that turn web APIs into acquisition-ready file manifests.

### Object storage protocols

For cloud object storage,
OpenECPDS supports multipart and resumable uploads,
chunk-size tuning,
endpoint overrides,
TLS settings,
bucket or container auto-creation,
and multiple authentication models.
Examples include S3 IAM role assumption and checksum controls,
Azure SAS tokens or managed identity,
and GCS service-account private key authentication with resumable chunked upload tuning.

See [Protocols & Connections](protocols.md),
[Object Storage](object-storage.md),
and the individual transfer-module pages linked from [Protocols & Connections](protocols.md).

## Related

- [Destination Options](destination-options.md)
- [Host Options](host-options.md)
- [Data User Options](../use-cases/data-portal-user-options.md)
- [Web User Options](web-user-options.md)
- [MQTT Overview](../notifications/mqtt-overview.md)
- [Protocols & Connections](protocols.md)
- [Object Storage](object-storage.md)
