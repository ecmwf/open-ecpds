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
| `WEATHER_SENDING` | Network Weather sending outcomes, including failures without socket measurements. |
| `WEATHER_CONNECTION` | Connection records associated with Network Weather measurements. |
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
| `STORAGE_LIFECYCLE` | Per-file lifecycle evaluation state, pinning details, eligibility times and reclamation results. |
| `STORAGE_POLICY_METRIC` | Cumulative reclaimed-byte and dropped-replica metrics, plus the last application time, per storage policy. |
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

This catalog is a database administration reference, not an end-user configuration
interface. Use the Monitor UI or supported APIs to manage operational records.
Table names and layouts can change between releases.

Do not load the destructive full installation dump into a populated database.
Schema operations must follow the site's backup and database change procedures.
Optional features report database-readiness problems on their administration pages.

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
