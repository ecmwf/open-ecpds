# Database

OpenECPDS uses a relational database to persist its operational records, configuration,
access control and monitoring history. This page is a logical catalog of the tables in
the base schema. It describes their purpose without listing columns or prescribing
database maintenance procedures.

The database image and the embedded standalone database use **MariaDB 11.4**.
The standalone installs the signed upstream 11.4 packages rather than the
distribution's older default packages. The standalone build verifies the server version.

The catalog reflects the schema defined in
[`docker/ecpds/database/ecpds.sql`](https://github.com/ecmwf/open-ecpds/blob/master/docker/ecpds/database/ecpds.sql).
Deployments with site-specific changes may have additional or different tables.
The standalone image build copies this schema into its build context; an existing
local copy there may be stale until the build context is refreshed.

The database image initializes new installations from `ecpds.sql`. The schema
includes the operational event, storage lifecycle and credential rotation tables.

## Transfers, files and products

| Table | Purpose |
|---|---|
| `DATA_FILE` | Registered data files, including their source, arrival details, size and file-level metadata. |
| `DATA_TRANSFER` | Transfer requests and their current scheduling, delivery, retry and expiry state. |
| `TRANSFER_HISTORY` | Timestamped transfer status and event history, including host, comments and errors. |
| `UPLOAD_HISTORY` | Records of completed data uploads and their transfer details. |
| `INCOMING_HISTORY` | History of incoming-user and Data Portal file operations. |
| `TRANSFER_STATISTICS` | Per-connection network statistics captured during a transfer. |
| `WEATHER_SENDING` | Network Weather sending outcomes, including failures without socket measurements. |
| `WEATHER_CONNECTION` | Connection records associated with Network Weather measurements. |
| `MONITORING_VALUE` | Timing values used to monitor expected, earliest, latest and predicted product arrival. |
| `PRODUCT_STATUS` | Current status and timing information for monitored products and cycles. |
| `PUBLICATION` | Scheduled publication or notification work associated with a transfer. |
| `MQTT_ACTIVITY` | MQTT publication outcomes and per-Mover broker results for the Activity dashboard. |
| `METADATA_ATTRIBUTE` | Names of metadata attributes that can be attached to data files. |
| `METADATA_VALUE` | Metadata attribute values associated with individual data files. |
| `PRODUCT_METADATA` | Product descriptions and tips, optionally scoped by product type. |

## Notification history and delivery

These tables support optional [notification history and replay](../notifications/history.md)
and durable MQTT CloudEvents delivery.

| Table | Purpose |
|---|---|
| `NOTIFICATION_HISTORY` | Captured file-availability events, including their destination, sequence, event identity and CloudEvents payload, for history and replay. |
| `NOTIFICATION_HISTORY_STATE` | Shared history sequence allocation, payload-byte accounting and timestamp state. |
| `NOTIFICATION_HISTORY_STREAM` | Per-destination sequence counters and removed-history boundaries used for replay gap detection. |
| `NOTIFICATION_CHECKPOINT` | Named consumer replay positions and acknowledgement timestamps per Data Portal user and destination. |
| `ACQUISITION_CHECKPOINT` | Replay positions and last inventory times for acquisition Hosts that replicate destinations via Aviso. |
| `NOTIFICATION_OUTBOX` | MQTT topics and CloudEvents payloads awaiting delivery to Data Mover brokers. |
| `NOTIFICATION_OUTBOX_STATE` | Shared pending-delivery payload-byte accounting and concurrency state for outbox capacity enforcement. |
| `NOTIFICATION_OUTBOX_TARGET` | Per-Mover pending deliveries, retry schedules, attempt counts and delivery-claim leases. |

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
| `HOST_OUTPUT` | The latest captured output and timestamp from host acquisition activity. |
| `HOST_STATS` | Host connection, transfer and check statistics. |
| `TRANSFER_GROUP` | Groups of hosts and destinations with shared transfer and replication settings. |
| `TRANSFER_METHOD` | Named transfer methods and their protocol-specific configuration. |
| `ECTRANS_MODULE` | Transfer modules available to the system. |
| `TRANSFER_SERVER` | Transfer server or Data Mover registrations and their group assignments. |
| `SCHEDULER_VALUE` | Scheduler-maintained host counters and runtime state. |
| `COUNTRY` | Country codes and display names used by destination and host records. |

## Host credential rotation

| Table | Purpose |
|---|---|
| `HOST_PASSWORD_ROTATION` | Pending vault credential references, staging state and probe results per Host. |
| `HOST_PASSWORD_PROBE_ATTEMPT` | Credential probe attempt timestamps used to enforce per-Host rate limits. |
| `HOST_PASSWORD_ACTIVATION` | Credential activation history, including the operator, endpoint and first confirmed delivery. |
| `HOST_PASSWORD_ACTIVE` | Active and previous vault credential references per Host. |
| `HOST_PASSWORD_ROTATION_GRANT` | Credential rotation permissions granted to Monitor users for specific destinations. |
| `HOST_PASSWORD_LEGACY_FENCE` | Per-Host blocks on new configured-credential operations and their drain status. |
| `HOST_PASSWORD_LEGACY_ADMISSION` | Configured-credential operations admitted before cutover and not yet confirmed closed. |
| `HOST_PASSWORD_LEGACY_CUTOVER` | Per-Host cutover records linking the staged credential to the approved endpoint and configuration. |
| `HOST_PASSWORD_LEGACY_ROUTE` | Mover routes enrolled in a cutover and their signed drain receipts. |
| `HOST_PASSWORD_LEGACY_RECONCILIATION` | Audit records for resolving uncertain cutovers using admission records and drain receipts. |
| `HOST_PASSWORD_DELIVERY` | Vault-backed transfer dispatches, their assigned Movers and delivery outcomes. |
| `HOST_PASSWORD_RETIREMENT` | Destruction status of superseded vault credential versions and references to their replacements. |

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
| `OPERATIONAL_EVENT` | Optional durable delivery outbox for native OpenTelemetry events; acknowledged records are deleted. |
| `SYSTEM_MESSAGE` | Scheduled system messages displayed to users. |
| `SYSTEM_PHYSICAL_LOCATION` | SNMP-collected physical locations and collection status per machine, including last attempt and success times. |
| `DELAY_NOTICE` | Registered delay notices and their status and content history. |
| `STORAGE_POLICY` | Storage lifecycle policy definitions and their matching criteria. |
| `STORAGE_LIFECYCLE` | Per-file lifecycle evaluation state, pinning details, eligibility times and reclamation results. |
| `STORAGE_POLICY_METRIC` | Cumulative reclaimed-byte and dropped-replica metrics, plus the last application time, per storage policy. |
| `SYS_CONFIG` | Named, shared system settings managed by OpenECPDS features. |
| `FEEDBACK` | Feedback submitted through the Monitoring Interface. |

## Statistics and traffic

| Table | Purpose |
|---|---|
| `BANDWIDTH` | Aggregated transfer volume, duration and file counts by destination and date. |
| `PORTAL_TRAFFIC` | Portal connection, denial, byte and duration totals at minute-level intervals, keyed by user. |
| `PORTAL_TRAFFIC_DAILY` | Daily aggregates of portal connections, denials, bytes and duration per user. |
| `PORTAL_BYTES_SNAPSHOT` | Minute-level portal upload and download byte snapshots per user. |
| `DESTINATION_BYTES_SNAPSHOT` | Minute-level upload and download byte snapshots per destination. |
| `MOVER_AVAILABILITY_SNAPSHOT` | Minute-level availability history for Data Movers. |

## How to read this catalog

This catalog is an implementation reference, not a configuration interface.
Use the Monitor UI, command-line tools or supported APIs to manage operational
records, including when acting as an administrator. Do not insert, update or
delete application records directly in the database. The application manages
these tables and their consistency.

Do not load the destructive full installation dump into a populated database.
Schema operations must follow the site's backup and database change procedures.
Optional features report database-readiness problems on their administration pages.

## Application caches

Hibernate second-level caching is configured in `conf/hibernate.hbm.xml`,
`conf/hibernate.cfg.xml` and `conf/ehcache.xml`. An entity with a cache mapping but
without an explicit Ehcache region uses the default template. Hibernate query
caching remains disabled.

The Master also caches the complete system-message list and destination metadata
catalog (field definitions and type restrictions) for 30 seconds. Returned objects
are copies, and message expiry is still evaluated against the current time on
each request. Administrative saves/deletes invalidate the corresponding local
snapshot; external database changes become visible on refresh.
Database read failures are not cached. Destination-specific metadata values and
transactional notification, outbox and lifecycle state are not added to these caches.

```ini
[ECpdsBase]
resultCacheTimeout=30s
```

Set this to `0` to disable these result caches. The database MBean's `clearCache`
operation clears both snapshots as well as Hibernate's cache. Existing Ehcache
region sizes and expiry policies are unchanged; Galera does not invalidate local
Java caches.

To collect Hibernate second-level cache hit/miss/put counters, enable:

```ini
[DataBase]
cacheStatistics=yes
```

The database MBean also exposes the writable `HibernateStatisticsEnabled`
attribute for temporary runtime collection and the read-only
`HibernateCacheStatistics` report. Collection is off by default, unless enabled
separately in Hibernate configuration. Counters are local to the Master and do
not include direct JDBC operations, result-cache hits or Ehcache evictions.
Disable collection after profiling if it is no longer needed.

## SQL script timeouts

Administrators maintaining named SQL scripts in `conf/sql` can specify a timeout in
whole seconds alongside their `#menu`, `#name` and `#group` directives:

```text
#timeout "10"
```

Without this directive, the default statement timeout is unchanged.
Use `#timeout "0"` to explicitly disable the statement timeout. Other database,
driver and connection limits still apply.
Use a single non-negative whole number of seconds. Invalid or duplicate directives
reject the request.

Timeouts apply to selects, counts, mapped-object queries and updates. For scripts
containing multiple statements or `LOOP`/`CHUNK` operations, the value applies to
each statement, not to the total script duration. Timeout enforcement depends on
the database driver; it does not limit connection waiting time.

After editing a loaded script, invoke `updateScriptContent(menu, group, name)` on
the Master's database MBean to refresh that script, or `reloadRepository()` to
reload the repository (including new scripts). The new SQL and timeout are used
by subsequent requests without restarting; already running statements are unchanged.
