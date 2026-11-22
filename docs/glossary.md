# Glossary

Key terms used in the OpenECPDS documentation and Monitor UI, grouped by topic.
For help finding a guide rather than a definition, see the [Documentation Guide](documentation-guide.md).

## Data, users and workflows

| Term | Definition |
|------|------------|
| **Alias** | A directional link that also queues transfers from one Destination at another. Conditions can limit which transfers are routed. See [Aliases](concepts/entities.md#aliases). |
| **Data Acquisition** | The service that automatically discovers and retrieves files from providers. See [Acquisition](use-cases/acquisition.md). |
| **Data Dissemination** | The service that automatically delivers data products to remote sites. See [Dissemination](use-cases/dissemination.md). |
| **Data File** | The record of a product, including its size and provider metadata. Several Data Transfers can reference the same Data File. See [Entities](concepts/entities.md#data-files-and-data-transfers). |
| **Data File ID** | The unique identifier assigned by the Master to a registered file. See [ECPDS command-line tool](use-cases/ecpds-cli.md). |
| **Data Portal** | The service through which remote users initiate uploads or downloads, using protocols such as SFTP, HTTPS, WebDAV, or S3. See [Data Portal](use-cases/data-portal.md). |
| **Data Store** | The OpenECPDS repository. It manages file metadata and can store file contents itself or retrieve them from a source when needed. See [Architecture Overview](architecture/overview.md). |
| **Data Transfer** | A transfer request linked to a unique data file, with schedule, priority, status, rate, errors, and history. See [Entities](concepts/entities.md#data-files-and-data-transfers). |
| **Data User (Incoming User)** | An account used to upload or download files through the Data Portal. See [Data Users](use-cases/data-users.md). |
| **Destination** | A logical queue and configuration for acquisition, dissemination, or Portal access. It can serve as a Portal bucket without a dissemination Host. See [Destinations](concepts/entities.md#destinations-and-aliases). |
| **ECtrans** | The transfer framework used for outgoing connections to remote systems. See [ECtrans common options](transfer-modules/ectrans.md). |
| **Fetch Mode** | Submission mode in which metadata is registered first and content is retrieved asynchronously. See [Transfer Lifecycle](architecture/data-transfer-lifecycle.md#fetch-mode). |
| **Host** | A configured endpoint and its connection, transfer, or file-selection rules. A Host is a configuration record, not necessarily a physical server. See [Host Types](concepts/host-types.md). |
| **Host Directory** | A Host field whose meaning depends on its type: for example, a remote path or a script. See [Host Directory](host-directory/index.md). |
| **Push Mode** | Submission mode in which metadata and file content are supplied directly. See [Transfer Lifecycle](architecture/data-transfer-lifecycle.md#push-mode). |
| **Standby** | A state in which a transfer is not picked up by the dissemination scheduler until activated. See [Transfer Lifecycle](architecture/data-transfer-lifecycle.md#data-portal). |
| **Transfer Method** | The configuration selected on a Host that identifies which Transfer Module to use. See [Transfer Methods](concepts/entities.md#transfer-methods). |
| **Transfer Module (ECtrans module)** | A pluggable implementation for an outgoing protocol or backend, such as FTP, SFTP, WebDAV, or S3. See [Transfer Modules](transfer-modules/index.md). |
| **Web User** | An account that uses the Monitor web interface; its permissions govern which data and actions it can access. See [Web Users](use-cases/web-users.md). |

## Architecture, scheduling and storage

| Term | Definition |
|------|------------|
| **Acquisition Host** | A Host that discovers and retrieves files from a source system using configured selection rules. See [Acquisition Hosts](concepts/host-types.md#acquisition). |
| **Acquisition Scheduler** | The scheduler that triggers remote file discovery on Acquisition Hosts. See [Discovery](use-cases/acquisition.md#discovery). |
| **Continental Data Mover** | A Data Mover deployed near receiving sites to shorten the final delivery path. See [Continental Data Movers](architecture/continental-data-movers.md). |
| **Dissemination Host** | A Host that defines where and how a Destination sends files to a target system. See [Dissemination Hosts](concepts/host-types.md#dissemination). |
| **ETag (entity tag)** | An object identifier used to distinguish content in the Data Store. See [Object Storage](concepts/object-storage.md). |
| **Host Failover** | Switching to another eligible Host when a connection fails. It is not whole-platform high availability. See [Failover](architecture/failover.md). |
| **Master Server** | The central coordinator: authentication, metadata, scheduling, Data Mover allocation. See [Components](architecture/components.md#master-server). |
| **Monitor Server** | The service that provides the web interface for monitoring and managing OpenECPDS. See [Components](architecture/components.md#monitor-server). |
| **Mover Server / Data Mover** | The service that transfers, stores, and streams file content. See [Components](architecture/components.md#mover-server-data-mover). |
| **Object Storage** | Storage that treats files as objects and uses metadata to present directory-like views. See [Object Storage](concepts/object-storage.md). |
| **Proxy Host** | A Host that associates a Continental Data Mover with a Destination for pre-replication and delivery. See [Proxy Hosts](concepts/host-types.md#proxy). |
| **Replication Host** | A Host that defines a location for additional copies of files. See [Replication Hosts](concepts/host-types.md#replication). |
| **Retrieval Scheduler** | Scheduler that retrieves file content for registered transfers (fetch mode and acquisition). See [Lifecycle](architecture/data-transfer-lifecycle.md). |
| **RMDCN** | Regional Meteorological Data Communication Network, a dedicated network used for institutional data exchange. See [Continental Data Movers](architecture/continental-data-movers.md). |
| **Server Plugin** | A pluggable implementation of a service that accepts incoming connections, such as HTTP or SSH. See [Plugin Infrastructure](concepts/plugin-infrastructure.md#server-plugins-incoming). |
| **Storage Lifecycle Policy** | Rules for managing redundant stored copies while protecting originals and active work. See [Storage Lifecycle Policies](administration/storage-lifecycle-policies.md). |
| **Storage Volume** | A filesystem location a Data Mover uses to store content. See [Volume Allocation](architecture/datamover-allocation.md#volume-filesystem-allocation). |
| **Target Data Mover** | In Data Portal workflows, the Data Mover where the file is stored. See [Data Portal](use-cases/data-portal.md). |
| **Transfer Group** | A named group of Data Movers used during allocation. See [Transfer Group Resolution](architecture/datamover-allocation.md#transfer-group-resolution). |
| **Transfer Scheduler** | The per-Destination scheduler that controls priorities, parallel transmissions, and retries. See [Destinations](concepts/entities.md#destinations-and-aliases). |
| **User Data Mover** | In Data Portal workflows, the Data Mover the remote user connects to. See [Data Portal](use-cases/data-portal.md). |

## Notifications and event-driven acquisition

| Term | Definition |
|------|------------|
| **Acquisition Checkpoint** | Saved progress on the receiving Master, used to resume one-way Aviso replication after a restart. See [Aviso Replication](notifications/aviso-acquisition.md). |
| **Aviso** | The interface through which authorised clients can watch or replay OpenECPDS file-availability events for an opted-in Destination. See [Aviso Client API](notifications/history.md#aviso-client-api). |
| **CloudEvents** | A standard format for event metadata and payloads, used for native file-availability events. See [Notification History](notifications/history.md). |
| **MQTT** | Lightweight publish/subscribe messaging used for OpenECPDS notifications and acquisition. See [MQTT Overview](notifications/mqtt-overview.md). |
| **MQTT Broker** | The server that receives MQTT messages from publishers and delivers them to matching subscribers. See [MQTT Overview](notifications/mqtt-overview.md). |
| **Notification History** | Bounded database storage for native file-availability events that can be replayed. It is separate from pending MQTT deliveries. See [Notification History and Replay](notifications/history.md). |
| **Notification Outbox** | Database-backed queue for pending native MQTT deliveries and retries. Delivery can produce duplicates. See [MQTT CloudEvents](notifications/history.md#mqtt-cloudevents). |
| **Notification Profile** | Rules that interpret incoming MQTT notifications for acquisition, such as the generic script profile or the WIS2 profile. See [Notification Profiles](concepts/plugin-infrastructure.md#notification-profiles). |
| **QoS (Quality of Service)** | MQTT delivery semantics. QoS 1 is at least once, so a client may receive a message more than once. See [MQTT CloudEvents](notifications/history.md#mqtt-cloudevents). |
| **Retain Flag** | Asks an MQTT broker to store the published message as the retained message for its topic and deliver it to new matching subscribers. It does not provide event history. See [Retained Messages](notifications/mqtt-overview.md#retained-messages-in-mqtt). |
| **Strict Capture** | A notification mode in which the transfer update and native event capture commit together before the Master acknowledges the update. See [Strict Capture](notifications/history.md#strict-capture). |
| **Topic / Topic Filter** | A topic names the address of a published MQTT message. Subscribers match topics using filters, which can include `+` and `#` wildcards. See [MQTT Overview](notifications/mqtt-overview.md). |
| **WIS2** | The WMO Information System 2.0, with which OpenECPDS integrates via MQTT. See [WMO WIS2](notifications/wmo-wis2.md). |

## Operations, security and integration

| Term | Definition |
|------|------------|
| **Delay Notification** | The Monitor workflow for preparing notifications about late or potentially late products. See [Delay Notification](monitoring/delay-notification.md). |
| **ECPDS CLI** | The command-line client for submitting files to OpenECPDS. See [ECPDS CLI](getting-started/ecpds-cli.md). |
| **Event Categories (PRS, RET, UPH, INH, ERR, CPY, DEA, NTF)** | Identifiers for structured operational log records. `NTF`, for example, records MQTT notification outcomes. See [Event Categories](event-logging/overview.md#event-categories). |
| **Hawtio** | A web console for browsing and managing Java components exposed through JMX. See [JMX Monitoring](monitoring/jmx-export.md). |
| **JMX (Java Management Extensions)** | The Java interface used to expose service metrics and management operations. See [JMX Monitoring](monitoring/jmx-export.md). |
| **Network Weather** | Optional diagnostics that combine transfer observations with network probes on approved paths. See [Network Weather](monitor-ui/admin.md#network-weather). |
| **OpenID Connect (OIDC)** | An optional way to sign in to the Monitor through an external identity provider. See [OpenID Connect Login](administration/openid-connect.md). |
| **OpenTelemetry (OTel)** | A standard way to collect and export logs, metrics, and traces. OTLP is the protocol used to send that telemetry. See [Event Logging](event-logging/overview.md). |
| **OpenTelemetry Journal** | An optional bounded database buffer that lets the service retry delivery of persisted operational events. It is not an audit trail or the Notification Outbox. See [Durable Database Journal](event-logging/overview.md#durable-database-journal). |
| **Prometheus** | A monitoring system that scrapes service metrics, including those exposed by the JMX exporter. See [JMX and Prometheus](monitoring/jmx-export.md). |
| **REST API** | The versioned HTTP interface for programmatic access to OpenECPDS resources. See [REST API](rest-api.md). |
| **TLS (Transport Layer Security)** | The protocol used to encrypt and authenticate supported connections, including HTTPS. See [TLS Certificates](administration/certificates.md). |
| **TOTP (Time-based One-Time Password)** | A short-lived code generated by an authenticator app. The configured authentication provider determines how it is used. See [TOTP Authentication](administration/totp.md). |

## Related

- [OpenECPDS Entities](concepts/entities.md)
- [Architecture Overview](architecture/overview.md)
- [Transfer Modules](transfer-modules/index.md)
