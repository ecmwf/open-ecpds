# Database

OpenECPDS uses a relational database to persist its operational records, configuration,
access control and monitoring history. This page is a logical catalog of the tables in
the base schema. It describes their purpose without listing columns or prescribing
database maintenance procedures.

The catalog reflects the schema defined in
[`docker/ecpds/database/ecpds.sql`](https://github.com/ecmwf/open-ecpds/blob/master/docker/ecpds/database/ecpds.sql).
The schema can change between releases; consult the source for the version you are
investigating. The table set may also differ where a deployment has site-specific changes.

## Transfers, files and products

| Table | Purpose |
|---|---|
| `DATA_FILE` | Registered data files, including their source, arrival details, size and file-level metadata. |
| `DATA_TRANSFER` | Transfer requests and their current scheduling, delivery, retry and expiry state. |
| `TRANSFER_HISTORY` | Historical transfer attempts and their outcomes. |
| `UPLOAD_HISTORY` | Records of completed data uploads and their transfer details. |
| `INCOMING_HISTORY` | History of incoming-user and Data Portal file operations. |
| `TRANSFER_STATISTICS` | Per-connection network statistics captured during a transfer. |
| `WEATHER_SENDING` | Immutable source/path, execution identity and terminal outcome for Network Weather's opt-in transactional socket writer, including failed sendings without socket rows. |
| `WEATHER_CONNECTION` | Stable connection identities linking sendings to socket statistics, with uniqueness for replay-safe transactional persistence and late-close supplements. |
| `MONITORING_VALUE` | Timing values used to monitor expected, earliest, latest and predicted product arrival. |
| `PRODUCT_STATUS` | Current status and timing information for monitored products and cycles. |
| `PUBLICATION` | Scheduled publication or notification work associated with a transfer. |
| `METADATA_ATTRIBUTE` | Names of metadata attributes that can be attached to data files. |
| `METADATA_VALUE` | Metadata attribute values associated with individual data files. |
| `PRODUCT_METADATA` | Product descriptions and tips, optionally scoped by product type. |

## Destinations, hosts and transfer configuration

| Table | Purpose |
|---|---|
| `DESTINATION` | Destination definitions and their delivery or acquisition settings. |
| `ALIAS` | Alternate destination names and their relationships to destinations. |
| `ASSOCIATION` | Associations between destinations and hosts, including priority. |
| `DESTINATION_META_FIELD` | Definitions for the configurable metadata fields shown on destinations. |
| `DESTINATION_META_FIELD_TYPE` | Destination types for which a metadata field is available. |
| `DESTINATION_META_VALUE` | Values of metadata fields for individual destinations. |
| `HOST` | Host definitions, including connection, protocol, role and operational settings. |
| `HOS_ECU` | Associations between hosts and ECPDS users. |
| `DES_ECU` | Associations between destinations and ECPDS users. |
| `HOST_LOCATION` | Host IP and geographic location information. |
| `HOST_OUTPUT` | The latest captured output from host checks or acquisition activity. |
| `HOST_STATS` | Host connection, transfer and check statistics. |
| `TRANSFER_GROUP` | Groups of hosts and destinations with shared transfer and replication settings. |
| `TRANSFER_METHOD` | Named transfer methods and their protocol-specific configuration. |
| `ECTRANS_MODULE` | Transfer modules available to the system. |
| `TRANSFER_SERVER` | Transfer server or Data Mover registrations and their group assignments. |
| `SCHEDULER_VALUE` | Scheduler-maintained host counters and runtime state. |
| `COUNTRY` | Country codes and display names used by destination and host records. |

## Users, access and API clients

| Table | Purpose |
|---|---|
| `WEB_USER` | Accounts used to sign in to the Monitoring Interface. |
| `CATEGORY` | Access-control categories used to group permissions. |
| `WEU_CAT` | Assignments of access-control categories to Monitoring Interface users. |
| `URL` | Protected Monitoring Interface URL resources. |
| `CAT_URL` | Assignments of URL resources to access-control categories. |
| `ECUSER` | ECPDS service and operational user accounts. |
| `INCOMING_USER` | Accounts used for incoming access and Data Portal services. |
| `INCOMING_ASSOCIATION` | Assignments of incoming users to destinations. |
| `INCOMING_PERMISSION` | File-operation permissions assigned to incoming users. |
| `INCOMING_POLICY` | Reusable incoming-access policies. |
| `POLICY_ASSOCIATION` | Assignments of incoming policies to destinations. |
| `POLICY_USER` | Assignments of incoming policies to users. |
| `OPERATION` | File operations that can be granted to incoming users. |
| `PORTAL_SUBSCRIBER` | Email subscriptions associated with incoming-user accounts. |
| `API_CLIENT` | API client identities and credential hashes. |
| `API_PERMISSION` | Service access patterns granted to API clients. |
| `API_EVENT` | API client usage and request outcomes. |

## Monitoring, administration and audit

| Table | Purpose |
|---|---|
| `ACTIVITY` | User activity context associated with recorded events. |
| `EVENT` | Operational actions, comments and errors associated with user activity. |
| `CHANGE_LOG` | Before-and-after records for changes to managed objects. |
| `SYSTEM_MESSAGE` | Scheduled system messages displayed to users. |
| `DELAY_NOTICE` | Registered delay notices and their status and content history. |
| `STORAGE_POLICY` | Storage lifecycle policy definitions and their matching criteria. |
| `SYS_CONFIG` | Named, shared system settings managed by OpenECPDS features. |
| `FEEDBACK` | Feedback submitted through the Monitoring Interface. |

## Statistics and traffic

| Table | Purpose |
|---|---|
| `BANDWIDTH` | Aggregated transfer volume, duration and file counts by destination and date. |
| `PORTAL_TRAFFIC` | Per-user portal connection, byte and duration totals at minute-level intervals. |
| `PORTAL_TRAFFIC_DAILY` | Daily aggregates of portal connections, bytes and duration per user. |
| `PORTAL_BYTES_SNAPSHOT` | Minute-level portal upload and download byte snapshots per user. |
| `DESTINATION_BYTES_SNAPSHOT` | Minute-level upload and download byte snapshots per destination. |
| `MOVER_AVAILABILITY_SNAPSHOT` | Minute-level availability history for Data Movers. |

## How to read this catalog

The table names are included as a reference for developers, administrators and people
investigating OpenECPDS. They are implementation details rather than part of the
end-user configuration interface. This catalog is not a column reference, entity
relationship diagram or database upgrade guide.

Network Weather remains disabled by default and never performs automatic DDL.
Fresh installations include its identity tables. Existing installations use the
additive `docker/ecpds/database/network-weather.sql` under the site's change
procedure, not the full fresh-install dump. Its online statistics covering-index
build reads existing data and requires DBA load/metadata-lock planning. The background capability check
requires the parent and identity tables, InnoDB, writer columns, unique identity
indexes and cascading foreign keys before allowing runtime activation.
Capacity limits are configured in the Master's `[NetworkWeather]` section and
reported to the Monitor. Fresh databases use `MEDIUMTEXT` for configuration documents;
existing databases need `docker/ecpds/database/network-weather-policy-capacity.sql`
before storing policies larger than the old 65,535-byte `TEXT` capacity. This optional
capacity change is separate from installing the Network Weather tables. See
[deployment capacity](../monitor-ui/admin.md#deployment-capacity).

For an existing database that has **neither** Network Weather migration applied,
`docker/ecpds/database/production-upgrade-20261018.sql` combines the new tables,
statistics index and configuration capacity change into one script. It covers only
the Network Weather schema delta, not earlier feature upgrades or fresh-install
seed data. Apply it once, without the client's `--force` option, after a backup and
DBA review. Do not combine it with the individual scripts on the same database:
duplicate objects intentionally cause an error. DDL auto-commits, so review any
partially applied run before retrying.
Ordinary delivery and the legacy successful-only writer do not require the new
tables. Tagged executions use the new writer exclusively; untagged executions
retain legacy behavior.

Sending headers, connection identities and statistics commit together; neither
volatile admission receipts nor assessment counters promise a committed ledger.
Reads of transfer-detail provenance are indexed by transfer ID and gated on
cached schema availability; they are not added to legacy history writes.
The identity tables cascade with transfer/socket deletion, preserving existing
purge behavior rather than introducing an indefinite history archive.

Automatic Network Weather selection stores its separate policy and bounded discovery
cursors under `SYS_CONFIG` (`NetworkWeather`, `automation`). Historical ranking is
explicitly opt-in: indexed seven-day upload keysets and destination/time samples
run only on its low-priority scheduler, never on a transfer callback or globe/admin
refresh. It borrows from the existing Hikari pool only with spare capacity, uses
a per-borrow 100-ms deadline, restores read-only state and applies two-second
statement timeouts within a 30-second run budget. Missing indexes, cancellation
failures and pool pressure are surfaced, not replaced with unrestricted queries.
Returned-row limits are not examined-row guarantees; representative query plans
and off/on load remain a test-system rollout gate.
