# Event Logging in OpenECPDS

OpenECPDS generates structured event logs that capture key activities and transitions
within the system, including data retrieval, replication, and dissemination processes.
These logs are designed to provide insight into system operations and facilitate
troubleshooting by recording events at different stages of the workflow.

At ECMWF, **Splunk** is used to collect, index, and analyse these event logs, enabling
efficient monitoring and alerting. However, OpenECPDS does not rely on Splunk specifically
— these logs follow a format that can be parsed by any log management or analysis tool,
such as ELK (Elasticsearch, Logstash, Kibana), Graylog, or Fluentd. This flexibility
allows different users and organisations to integrate OpenECPDS logs into their own
monitoring and analytics ecosystems.

The logs can be found in the directories mounted to the containers — see
[Checking the Containers and Logs](../getting-started/first-run.md#checking-the-containers-and-logs).

## Choosing event outputs

The Master publishes the same operational events through two independent outputs:

- **Splunk file**: the `SplunkLogs` Log4j2 logger writes `splunk.log`.
  Records use category codes and ordered text fields; Log4j2 controls file
  rotation.
- **Native OpenTelemetry**: the application supplies named, typed attributes
  directly to an OpenTelemetry Logs SDK. It does not tail or parse `splunk.log`.
  Records are exported over OTLP to a Collector or compatible platform.

Configure the Master in `etc/master/conf/ecmwf.properties`:

```ini
[OperationalEvents]
splunkEnabled=yes
openTelemetryEnabled=no
```

| Required output | `splunkEnabled` | `openTelemetryEnabled` |
|-----------------|-----------------|------------------------|
| Neither | `no` | `no` |
| Splunk file only (default) | `yes` | `no` |
| OpenTelemetry only | `no` | `yes` |
| Both | `yes` | `yes` |

Restart the Master after changing these settings or the OpenTelemetry
configuration. These switches control operational event output only: ordinary
service logs, database history and the Monitor change log are unaffected.
Log4j2 may create an empty `splunk.log` file when its appender starts.

The file output also honours the `SplunkLogs` INFO logging level.
Disabling that logger does **not** disable OpenTelemetry. Event-specific
gates still apply to both outputs, including the transfer's `recordUPH` selection
and the Data User's `portal.recordSplunk` and `portal.triggerEvent` settings.
The `portal.recordSplunk` setting controls INH event generation, not selection
of the output platform.

### OpenTelemetry connection

Enable `openTelemetryEnabled` and supply standard OpenTelemetry environment
variables to the **Master process**, for example:

```sh
OTEL_SERVICE_NAME=open-ecpds-master
OTEL_EXPORTER_OTLP_LOGS_PROTOCOL=http/protobuf
OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=https://collector.example.org:4318/v1/logs
OTEL_EXPORTER_OTLP_LOGS_TIMEOUT=10000
OTEL_BLRP_MAX_QUEUE_SIZE=2048
OTEL_BLRP_MAX_EXPORT_BATCH_SIZE=512
OTEL_BLRP_SCHEDULE_DELAY=1000
```

Export these variables in the service environment or pass them into the Master
container. JVM system properties such as
`-Dotel.exporter.otlp.logs.endpoint=...` are also supported through `JAVA_OPTS`.
Merely setting variables in the host shell does not pass them into an existing
container or systemd service.

The default protocol is `http/protobuf`; without an endpoint override its SDK
default is the local Collector at `http://localhost:4318/v1/logs`. Set
`OTEL_EXPORTER_OTLP_LOGS_PROTOCOL=grpc` to use a gRPC receiver with an appropriate
endpoint, normally port 4317 and no `/v1/logs` suffix. A logs-specific HTTP
endpoint must include the full path; the generic `OTEL_EXPORTER_OTLP_ENDPOINT`
instead supplies a base URL.

For authentication, inject `OTEL_EXPORTER_OTLP_LOGS_HEADERS` through your secret
management system. For a private Collector CA, use the SDK's
`OTEL_EXPORTER_OTLP_LOGS_CERTIFICATE` setting. Use HTTPS for non-local traffic;
do not disable certificate validation. Never commit credentials to configuration
or include authorization headers in diagnostic output.

The SDK is private to this event publisher and is not registered as the global
OpenTelemetry SDK. It exports logs only; it does not enable tracing, instrument
application methods, or replace Prometheus/JMX metrics. Standard
`OTEL_LOGS_EXPORTER` settings apply, with `otlp` as the default. Invalid SDK
configuration prevents startup rather than silently falling back to another
output. When OpenTelemetry is disabled, its exporters are not initialized.

### Native record schema

Each record has:

- Event name `ecpds.<category>`, for example `ecpds.UPH`, and body `UPH`.
- Attribute `ecpds.event.category` containing the category code.
- Attribute `ecpds.event.id` identifying the event; journal retries preserve it.
- The field names listed below as attributes, preserving their original case.
  Booleans and integral/floating-point Java values use native OTLP types;
  textual fields remain strings. The `none`, `-` and `null` placeholders
  retain their textual meaning.
- The original `TimeStamp` as the log timestamp and as a textual attribute.
  Other date/time fields use their category's textual formatting.
- Resource fields `service.name` (default `open-ecpds-master`),
  `service.version`, and `service.instance.id` identifying the Master.
  Explicit `OTEL_RESOURCE_ATTRIBUTES` values can override the version and
  instance defaults.

Values containing semicolons or equals signs remain individual native
attributes; they are not split into text fields. OpenTelemetry SDK attribute
limits still apply. Treat filenames, user identifiers, addresses, comments and
other event fields as potentially sensitive and apply appropriate
Collector access controls, filtering and retention. No full configuration
snapshot or password/token field is included in these records.

### Delivery and failure handling

Without database journaling, OpenTelemetry uses the SDK's asynchronous batch processor, bounded in-memory
queue and exporter retry behaviour. Event producers do not wait for a remote
Collector. Completed export failures are reported in the Master service log
with rate-limited lost-record counts; SDK diagnostics provide further details.
Master shutdown attempts to drain pending records, with a bounded wait.

Delivery is **best effort**, not transactional or exactly-once. A full queue,
an exhausted export attempt, a process crash or an abrupt shutdown can lose
records; retries may also create duplicates. SDK queue drops are not included in
the publisher's export-failure count. For stronger durability, enable the
[database journal](#durable-database-journal), enable the Splunk file and/or use
a local Collector with persistent queues, and monitor the pipeline's health.
The outputs are independent, not automatic failover:
OpenTelemetry failure does not turn file logging on if it was disabled.

### Durable database journal

For recovery during endpoint outages and Master restarts, enable the optional
database journal. It stores each native event **before** attempting delivery,
not only after an export has already failed. The `splunk.log` output remains
independent and is not replayed by this feature.

Journaling requires the `OPERATIONAL_EVENT` table in the Master's database.
The supplied installation schema includes this table. The Master's `conf/sql`
directory must contain `insertOperationalEvent.sql`, `getOperationalEvents.sql`
and `deleteOperationalEvent.sql`. The running service does not create tables.

Then configure and restart the Master:

```ini
[OperationalEvents]
splunkEnabled=yes
openTelemetryEnabled=yes
journalEnabled=yes
journalOwner=production-master
```

Supply the OTLP endpoint, authentication and TLS settings as described above.
Journal mode requires `OTEL_LOGS_EXPORTER=otlp` (the default), with one OTLP
exporter. To fan out to several platforms, use a Collector downstream.
Setting `OTEL_SDK_DISABLED=true` while journaling is enabled is a configuration
error, rather than silently bypassing persistence.

**Keep `journalOwner` stable and unique to the logical Master.** If omitted, it
defaults to `Login.root`, then `Login.hostName`, then `master`. Only that owner's
records are replayed. A failover instance must use the same owner and database,
with the previous instance stopped; run only one active Master per owner.
Changing the owner leaves the previous owner's pending rows untouched.

The journal preserves the original event timestamp, native attribute types and
resource identity. Each journalled record includes a stable `ecpds.event.id`
attribute for downstream deduplication. A background worker reads up to 256
oldest pending records at a time, normally polling once per second. It exports
outside the business-operation thread and deletes rows only when the OTLP
exporter reports success. Failed batches are retained and retried after a
five-second pause, in addition to the exporter's own transient-failure retries.
An acknowledged batch whose database deletion fails is retained until deletion
can succeed; restarting in that interval may deliver duplicates.

In journal mode, the database is the pending queue: `OTEL_BLRP_*` settings apply
only to the best-effort fallback path, not to normal journal delivery.
Shutdown need not drain the database; the next start resumes pending work.
Disabling OpenTelemetry or journaling pauses replay without deleting pending
rows. Re-enabling it with the same owner resumes replay.

#### Guarantees and operational limits

- **Endpoint outages:** successfully persisted, unacknowledged events have no
  automatic expiry or retry limit, including permanent HTTP errors. Fix the
  endpoint or credentials; an indefinitely rejected batch can block the backlog.
  An undecodable row is also retained with an error for operator investigation.
- **Database failures:** operations continue, a prominent rate-limited
  `DATABASE EVENT JOURNAL UNAVAILABLE` error reports persistence failures, and
  the application attempts best-effort delivery through the SDK's memory queue.
  Those events are **not guaranteed durable** and are not automatically
  backfilled later. An ambiguous database acknowledgement can also result in a
  persisted record and a best-effort delivery of the same event.
- **Duplicates and acknowledgements:** delivery is at-least-once for persisted
  events while the endpoint rejects failed requests and eventually accepts
  them. Deduplicate using `ecpds.event.id` where needed. An OTLP acknowledgement
  is not proof of indexing or durable storage in the final platform. In
  particular, the SDK can report success for an OTLP partial-success response;
  individually rejected records cannot be identified and replayed by this
  journal. Avoid rejecting/filtering required records downstream and monitor
  Collector/backend rejection diagnostics.
- **Business transactions:** journal insertion is a separate synchronous
  database operation, not part of the transfer's transaction. A crash before
  insertion can still leave a completed operation without an event. This is a
  durable delivery outbox, not a transactional audit trail.
- **Capacity and latency:** admission adds one database write to the producer's
  path. Journal SQL statements have a five-second timeout; connection-pool
  waiting and driver/network limits still apply. Backlog growth is intentionally
  not capped or silently purged. Size database storage and recovery throughput
  for expected event rates and outage duration, and monitor database load.
- **Storage protection:** database durability, replication and backups remain
  essential. Protect journal payloads using the same access and retention
  controls as operational logs. Successfully acknowledged events are deleted;
  this table is not a permanent history archive.

Monitor pending count and oldest admission time for the configured owner, for
example through the site's database monitoring:

```sql
SELECT COUNT(*) AS pending, MIN(OEV_CREATED_AT) AS oldest_pending
FROM OPERATIONAL_EVENT
WHERE OEV_OWNER = 'production-master';
```

Alert on growing backlog age/count, journal/export error messages, database disk
space and rejected records downstream. Do not routinely delete unacknowledged
rows: doing so intentionally discards events. A local Collector with persistent
queues remains useful to protect the next hop after the journal receives an
OTLP acknowledgement.

The publisher exports the seven categories listed below. Operational events
and the Monitor's database change log are separate records.

## Event Categories

OpenECPDS generates various types of events, each categorised based on the nature of the
operation being recorded:

| Code | Category | Description | Fields |
|------|----------|-------------|--------|
| **PRS** | PRoduct Status | Tracks the status of products as they progress through OpenECPDS. The system maintains a top-level monitoring display, which can be updated using the `ecpds` command. Events are logged at key stages, including when a set of files is scheduled, when files start being registered, and when they are fully recorded. | [PRS fields](prs-fields.md) |
| **RET** | RETrieval | Corresponds to the retrieval process. Once a file is registered, it is handled by the Retrieval Scheduler. Upon completion (successful or not), a RET event records the outcome. | [RET fields](ret-fields.md) |
| **UPH** | UPload History | Records events whenever a file is successfully disseminated from OpenECPDS to a remote site. Tracks outbound file transfers. | [UPH fields](uph-fields.md) |
| **INH** | INcoming History | Logs incoming data transfers — files pushed to OpenECPDS or retrieved by users via the data portal. | [INH fields](inh-fields.md) |
| **ERR** | ERRor | Captures error events, specifically when a file fails to be disseminated successfully. | [ERR fields](err-fields.md) |
| **CPY** | CoPY / Replication | Tracks file replication between different data movers. Records whether the replication was successful. | [CPY fields](cpy-fields.md) |
| **DEA** | DEnied Access | Captures unauthorised access attempts, helping administrators identify security threats, misconfigurations, or unexpected behaviour. | [DEA fields](dea-fields.md) |

## Field formats

Each event logged by OpenECPDS follows a structured format, with fields that provide
detailed information about the recorded operation. The fields vary depending on the event
category, capturing relevant attributes such as timestamps, file identifiers, processing
statuses, and transfer details.

!!! note
    All duration values are measured in **milliseconds**. All timestamps are formatted as
    `yyyy-MM-dd HH:mm:ss.fffffffff`, where `yyyy` is the 4-digit year, `MM` is the
    2-digit month, `dd` is the 2-digit day, `HH` is the 2-digit hour (24-hour clock),
    `mm` is minutes, `ss` is seconds, and `fffffffff` represents fractional seconds in
    nanoseconds.

!!! note
    A `TimeStamp` field is included in all Splunk log entries to ensure log rotation is
    correctly detected. This prevents Splunk from misidentifying rotated files due to
    identical file headers and avoids missing or duplicated log events.

## Field references by category

- [PRS (Product Status)](prs-fields.md)
- [RET (Retrieval)](ret-fields.md)
- [UPH (Upload History)](uph-fields.md)
- [INH (Incoming History)](inh-fields.md)
- [ERR (Error)](err-fields.md)
- [CPY (Copy / Replication)](cpy-fields.md)
- [DEA (Denied Access)](dea-fields.md)

## Related

- [Lifecycle of a Data Transfer](../architecture/data-transfer-lifecycle.md)
- [ECPDS command-line Tool](../use-cases/ecpds-cli.md)
