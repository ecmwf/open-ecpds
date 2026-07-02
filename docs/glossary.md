# Glossary

Key terms used throughout the OpenECPDS documentation. Where a term has a dedicated page,
a link is provided.

| Term | Definition |
|------|------------|
| **Data Store** | The multi-purpose repository at the heart of OpenECPDS. It does not necessarily store data physically but works like a search engine, crawling and indexing metadata, while optionally caching content. See [Architecture Overview](architecture/overview.md). |
| **Data Acquisition** | The active service that automatically discovers and retrieves data from data providers. See [Acquisition](use-cases/acquisition.md). |
| **Data Dissemination** | The active service that automatically distributes data products to remote sites. See [Dissemination](use-cases/dissemination.md). |
| **Data Portal** | The passive service that lets remote sites push and pull data (FTP, HTTPS, S3). See [Data Portal](use-cases/data-portal.md). |
| **Data File** | A record of a product stored in the Data Store, with a one-to-one mapping to the product, holding its physical specifications and provider metadata. See [Entities](concepts/entities.md#data-files-and-data-transfers). |
| **Data Transfer** | A transfer request linked to a unique data file, with schedule, priority, status, rate, errors, and history. See [Entities](concepts/entities.md#data-files-and-data-transfers). |
| **Data File ID** | A unique identifier allocated by the Master Server for a registered file, used to track it. See [ECPDS command-line Tool](use-cases/ecpds-cli.md). |
| **Destination** | A place where data transfers are queued and processed to deliver data to a unique remote place. See [Entities](concepts/entities.md#destinations-and-aliases). |
| **Alias** | A link between two or more destinations so data queued to one is also queued to the others (optionally conditional). See [Entities](concepts/entities.md#aliases). |
| **Dissemination Host** | A host that connects and transmits a data file's content to a target system. See [Entities](concepts/entities.md#dissemination-and-acquisition-hosts). |
| **Acquisition Host** | A host that discovers and retrieves files from a source system, including file selection rules. See [Entities](concepts/entities.md#dissemination-and-acquisition-hosts). |
| **Transfer Method** | The configuration on a host that determines which transfer module (protocol) is used. See [Entities](concepts/entities.md#transfer-methods). |
| **Transfer Module (ECtrans module)** | A pluggable implementation of a protocol or storage backend (FTP, SFTP, S3, etc.). See [Transfer Modules](transfer-modules/index.md). |
| **Master Server** | The central coordinator: authentication, metadata, scheduling, Data Mover allocation. See [Components](architecture/components.md#master-server). |
| **Mover Server / Data Mover** | The service that moves bytes — connecting to remote systems via transfer modules, storing and streaming content. See [Components](architecture/components.md#mover-server-data-mover). |
| **User Data Mover** | In Data Portal workflows, the mover the customer connects to. See [Data Portal](use-cases/data-portal.md). |
| **Target Data Mover** | In Data Portal workflows, the mover where the file is stored. See [Data Portal](use-cases/data-portal.md). |
| **Continental Data Mover** | A standard Data Mover with limited functionality and an alternative communication module, deployed near target sites to optimise dissemination. Configured via a Proxy Host. See [Continental Data Movers](architecture/continental-data-movers.md). |
| **Proxy Host** | A host defined to associate a Continental Data Mover with a destination for pre-replication and communication. See [Replication directory](host-directory/replication.md). |
| **Monitor Server** | The web-based monitoring and management interface. See [Components](architecture/components.md#monitor-server). |
| **Transfer Scheduler** | Per-destination scheduler controlling priorities, parallel transmissions, and retries. See [Entities](concepts/entities.md#destinations-and-aliases). |
| **Retrieval Scheduler** | Scheduler that retrieves file content for registered transfers (fetch mode and acquisition). See [Lifecycle](architecture/data-transfer-lifecycle.md). |
| **Acquisition Scheduler** | Scheduler that triggers discovery of remote files on acquisition hosts. See [Acquisition](use-cases/acquisition.md#discovery). |
| **Push Mode** | Submission mode where both metadata and content are pushed directly. See [Lifecycle](architecture/data-transfer-lifecycle.md#push-mode). |
| **Fetch Mode** | Submission mode where metadata is submitted first and content retrieved asynchronously. See [Lifecycle](architecture/data-transfer-lifecycle.md#fetch-mode). |
| **Failover** | The mechanism for switching between hosts when a connection fails. See [Failover](architecture/failover.md). |
| **MQTT** | Lightweight publish/subscribe messaging used for OpenECPDS notifications and acquisition. See [MQTT Overview](notifications/mqtt-overview.md). |
| **Topic** | An MQTT addressing string (supporting `+` and `#` wildcards) used to route notifications. See [MQTT Overview](notifications/mqtt-overview.md). |
| **Retain flag** | An MQTT flag that keeps the most recent message available for late subscribers. See [MQTT Overview](notifications/mqtt-overview.md#retained-messages-in-mqtt). |
| **WIS2** | The WMO Information System 2.0, with which OpenECPDS integrates via MQTT. See [WMO WIS2](notifications/wmo-wis2.md). |
| **ETag** | An entity tag identifying a stored object's content in the Data Store. See [Object Storage](concepts/object-storage.md). |
| **Event categories (PRS, RET, UPH, INH, ERR, CPY, DEA)** | Structured log event types capturing system activity. See [Event Logging](event-logging/overview.md). |
| **RMDCN** | A dedicated network used for institutional data exchange (referenced as a NetworkCode). See [Continental Data Movers](architecture/continental-data-movers.md). |
| **Standby** | A submitted transfer flagged to be ignored by the Data Transfer Scheduler until activated. See [Lifecycle](architecture/data-transfer-lifecycle.md#data-portal). |

## Related

- [OpenECPDS Entities](concepts/entities.md)
- [Architecture Overview](architecture/overview.md)
- [Transfer Modules](transfer-modules/index.md)
