# Event Logging in OpenECPDS

OpenECPDS generates structured event logs that capture key activities and transitions
within the system, including data retrieval, replication, and dissemination processes.
These events are available through both structured file logging and native
**OpenTelemetry** export, providing insight into operations and supporting
troubleshooting at different stages of the workflow.

At ECMWF, different platforms use **Splunk** and **Fluent Bit** pipelines.
The structured file output supports existing Splunk collection and analysis,
while native OpenTelemetry exports events over OTLP to compatible collectors,
including Fluent Bit with an OTLP input configured. Both outputs can be enabled
independently or used together. OpenTelemetry also supports correlation with
application traces and metrics.

OpenECPDS is not tied to a particular backend: file collectors and OTLP-compatible
pipelines can route events into each organisation's monitoring and analytics
ecosystem.

File logs can be found in the directories mounted to the containers — see
[Checking the Containers and Logs](../getting-started/first-run.md#checking-the-containers-and-logs).

!!! note "Structure, format and standards"
    **The `splunk.log` events are structured logs in an application-specific
    encoding.** A custom encoding is not the same as an absence of structure.
    The following distinctions apply to both the file output and native
    OpenTelemetry:

    | Criterion | What it means | How `splunk.log` meets it |
    |-----------|---------------|--------------------------|
    | Identifiable fields | Consumers can distinguish the event category and individual named fields. | Each record starts with a category code, followed by ordered `Name=value` fields separated by `;`. |
    | Consistent semantics | A field has a defined meaning within its event category. | Category-specific field definitions describe identifiers, outcomes, timestamps, sizes and other event attributes. |
    | Documented schema | Consumers know which fields to expect and how to interpret them. | Each event-category page provides a Fields table with names, types and meanings, alongside example records. |
    | Defined value types | Values can be interpreted as timestamps, booleans, integers or strings according to the schema. | The file represents values as text; consumers apply the documented types. Native OpenTelemetry preserves supported numeric and boolean attribute types directly. |
    | Unambiguous field boundaries | Data cannot be mistaken for separators or extra records. | File records containing `;`, CR or LF in a category, field name or value are rejected with a WARN rather than written ambiguously. Field names cannot contain `=`; parsers split each `Name=value` field at its first `=`, preserving equals signs within values. |
    | Defined record boundaries | Consumers can identify where one event ends and the next begins. | Each accepted event occupies one line. Embedded CR/LF is rejected for the file output. |
    | Machine processing | Fields can be extracted consistently without interpreting free-form prose. | A parser reads the category, splits fields on `;`, then splits each field at its first `=` and applies the category schema. File collection supports existing Splunk forwarder pipelines. |
    | Standardisation | The encoding and schema may be application-specific or defined by an external specification. | The semicolon-delimited encoding and ECPDS event schema are application-specific, not a claim of JSON, syslog or OTLP conformance. Native OpenTelemetry provides the alternative standard telemetry data model and OTLP export; ECPDS-specific event attributes remain application-defined. |

    A flat, single-line representation does not remove event structure, and
    nesting is not required for structured logging. Likewise, using familiar
    value types does not make an application-specific encoding an external
    standard. Structure, encoding, schema and standardisation are related but
    separate properties.

    The practical trade-off is interoperability: existing Splunk pipelines can
    continue using their established file parser, whereas other consumers must
    implement that parser or use native OpenTelemetry. A Splunk forwarder can
    collect other file encodings too; the current format is an integration choice,
    not a requirement imposed by Splunk.

    Delimiter validation protects the file representation without modifying
    values or changing its established encoding. A rejected file record is absent
    from `splunk.log`; its native OpenTelemetry event can still be emitted when
    that output is enabled. This is an encoding safeguard, not a guarantee of
    delivery or a substitute for monitoring collection failures.

## Reading the examples

Each event-category page has **Splunk (`splunk.log`)** and **OpenTelemetry**
tabs showing the same event and sample field values. The shared Fields table
describes both outputs. The examples are illustrative, not captured production
records; existing sample timestamps are retained and do not necessarily describe
a chronological transfer lifecycle.

The OpenTelemetry tab is a **simplified Collector/backend representation, not
the raw OTLP payload**. Actual export uses OTLP and batches records with resource
and instrumentation-scope metadata. OTLP JSON, when used for inspection, has
additional wrappers such as `stringValue`, `intValue` and `boolValue`; backend
field layout can differ. Resource attributes such as `service.version` and SDK
metadata are omitted from the examples for readability.

Unlike the semicolon-separated file output, telemetry attributes retain their
application types: counts and sizes are numbers, flags are booleans, and textual
identifiers remain strings even if they contain only digits. Timestamp-valued
event attributes retain their formatted string representation. The `TimeStamp`
value represents an absolute instant. In OpenTelemetry, the log record's
top-level timestamp is set from that instant and is timezone-independent;
backends commonly display it in UTC. The `TimeStamp` attribute itself, like
the legacy Splunk field, is formatted without a UTC offset using the Master
JVM's default timezone. The examples assume that timezone is UTC, so their
attribute value and top-level timestamp show the same wall-clock time. Prefer
the top-level OpenTelemetry timestamp for cross-service correlation; a
timezone-less `TimeStamp` string cannot identify its timezone by itself.
Durations remain milliseconds where specified by the Fields table. These
records all use log severity `INFO`, including the `ERR` and `DEA` categories:
the category and event fields describe the operational outcome.

| Telemetry metadata | Meaning |
|--------------------|---------|
| `resource.service.name` | Exporting service; defaults to `open-ecpds-master`. |
| `resource.service.instance.id` | Master identity, normally derived from `[Login] root` or `hostName`. |
| `scope.name` | `ecmwf.ecpds.operational-events`, the instrumentation scope. |
| `eventName` / `body` | `ecpds.<category>` / `<category>`, for example `ecpds.PRS` / `PRS`. |
| `attributes.ecpds.event.id` | Generated UUID identifying the telemetry event; useful for deduplicating journal retries. |
| `attributes.ecpds.event.category` | The category code, matching the file output's first field. |
| `attributes.ecpds.transfer.attempt.id` | Present on attempt-aware UPH/ERR events when a valid transfer context is available; distinguishes attempts of the same transfer. |
| `attributes.ecpds.retrieval.id` | Present on RET events when retrieval tracing supplies a valid retrieval context; links all related transfer records for the same DataFile retrieval. |
| `traceId`, `spanId` | Optional valid trace context linking a log to a span; a corresponding trace is not guaranteed to be sampled or retained. |

The UPH example illustrates optional correlation. Product-status (PRS),
retrieval (RET), incoming-history (INH), copy (CPY) and denied-access (DEA)
events are not automatically assigned a dissemination-attempt context simply
because they describe a file or contain a transfer ID. INH logs for actual
portal uploads/downloads can instead link to a portal-transfer span.
See [Transfer traces and log correlation](#transfer-traces-and-log-correlation)
for tracing scope and configuration.

## Choosing event outputs

The Master publishes the same operational events through two independent outputs:

- **Splunk file**: the `SplunkLogs` Log4j2 logger writes `splunk.log`.
  Records use category codes and ordered text fields; Log4j2 controls file
  rotation. A category, field name or value containing a semicolon, carriage
  return or newline causes the entire file record to be rejected with a WARN
  in the Master service log. Field names containing `=` are also rejected;
  values may contain `=` (split each field at its first equals sign).
  Warnings do not include offending content. This validation does not reject
  the independent native OpenTelemetry event or interrupt application operations.
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
variables to the **Master process**. For a service installation, put connection
settings in `/etc/ecpds/master.cnf`, which the Master launcher sources:

```sh
export OTEL_EXPORTER_OTLP_ENDPOINT="https://collector.example.org:4318"
export OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
export OTEL_EXPORTER_OTLP_TIMEOUT="10000"
# Optional private Collector CA:
# export OTEL_EXPORTER_OTLP_CERTIFICATE="/etc/ecpds/master/collector-ca.pem"
```

Use `export` for each environment setting in `master.cnf` so Java inherits it.
Any supported `OTEL_*` setting can be supplied this way without editing the
launcher. Headers stay in the environment rather than the launcher's generated
JVM argument file.

The Master's `ecmwf.properties` contains output switches and the journal owner
in `[OperationalEvents]`, and batching defaults in:

```ini
[OpenTelemetry]
logsMaxQueueSize=2048
logsMaxExportBatchSize=512
logsScheduleDelayMillis=1000
```

Precedence for these batching settings is **JVM system properties → environment
variables → `ecmwf.properties` → built-in defaults**. Their standard overrides are
`otel.blrp.max.queue.size` / `OTEL_BLRP_MAX_QUEUE_SIZE`,
`otel.blrp.max.export.batch.size` / `OTEL_BLRP_MAX_EXPORT_BATCH_SIZE`, and
`otel.blrp.schedule.delay` / `OTEL_BLRP_SCHEDULE_DELAY`. Delay is in milliseconds.
These settings are optional; the values shown above are the defaults.
With journaling disabled, these settings tune the normal in-memory log queue.
With journaling enabled, a separate bounded admission queue and per-owner database
limits apply instead (see below); persistence failures drop new telemetry rather
than sending it through an alternate fallback queue. Restart the Master after changes.
The SDK does not read arbitrary `OTEL_*` keys from `ecmwf.properties`.

For containers, pass these variables through the container environment, or mount
`master.cnf` at the path above. JVM system properties such as
`-Dotel.exporter.otlp.endpoint=...` are also supported through `JAVA_OPTS`.
Merely setting variables in the host shell does not pass them into an existing
container or systemd service.

The default protocol is `http/protobuf`; without an endpoint override its SDK
default is the local Collector at `http://localhost:4318/v1/logs`. Set
`OTEL_EXPORTER_OTLP_PROTOCOL=grpc` to use a gRPC receiver with an appropriate
endpoint, normally port 4317 and no `/v1/logs` suffix. A logs-specific HTTP
endpoint must include the full path; the generic `OTEL_EXPORTER_OTLP_ENDPOINT`
instead supplies a base URL. For HTTP/protobuf, the SDK appends `/v1/logs` to
that base URL. Logs-specific settings override their generic counterparts when
both are configured. The Master service name defaults to `open-ecpds-master`;
`OTEL_SERVICE_NAME` is an optional override.

For authentication, inject `OTEL_EXPORTER_OTLP_HEADERS` through your secret
management system. For a private Collector CA, use the SDK's
`OTEL_EXPORTER_OTLP_CERTIFICATE` setting. Use HTTPS for non-local traffic;
do not disable certificate validation. Never commit credentials to configuration
or include authorization headers in diagnostic output.

Transfer tracing is enabled independently as described below;
neither output replaces Prometheus/JMX metrics. Standard
`OTEL_LOGS_EXPORTER` settings apply, with `otlp` as the default. Invalid SDK
configuration prevents startup rather than silently falling back to another
output. When OpenTelemetry is disabled, its exporters are not initialized.

### Transfer traces and log correlation

Optional distributed traces follow **one scheduled dissemination attempt**, not the
lifetime of a file or its complete retry history. Enable them in `[OpenTelemetry]`
in the **Master's** `ecmwf.properties`:

```ini
[OpenTelemetry]
tracesEnabled=yes
tracesSampleRatio=0.01
tracesMaxQueueSize=2048
tracesMaxExportBatchSize=512
tracesScheduleDelayMillis=1000
```

Restart the Master after changes. Tracing is off by default and is independent of
`[OperationalEvents] openTelemetryEnabled`: traces can be exported without native
logs, and logs can be exported without traces. To use both, enable both switches
on the Master. Movers automatically follow the tracing decision supplied with
each transfer. No OpenTelemetry settings, Collector credentials or direct
Collector connections are required on Movers or Continental relays.

Use the generic OTLP connection settings above in `master.cnf`.
Only the Master exports logs and traces to the Collector. With HTTP/protobuf the base endpoint supplies
`/v1/traces` for spans and `/v1/logs` for logs. If a platform has separate ingest
URLs, set `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` and
`OTEL_EXPORTER_OTLP_LOGS_ENDPOINT` to the full signal-specific URLs.
Service names default to `open-ecpds-master` and `open-ecpds-mover`; Mover spans
retain their originating service and instance identity.

Movers send completed spans asynchronously in batches through their existing
Master connection. Continental Movers use the existing REST relay path.
Forwarding queues are bounded and held in memory; connection failures are
retried while the process is running, but full queues and shutdown can discard
spans. Check Mover, relay and Master service logs for forwarding/export errors.
Trace forwarding does not block transfers. The Master, Movers and Continental
relays must use matching versions for the retrieval-span wire format.

Traces cover dispatch, transfer execution and completion for dissemination
attempts. Retries have separate traces. UPH and dissemination ERR native logs
include OTLP `trace_id` and `span_id` fields linking to the relevant span;
`ecpds.transfer.attempt.id` also identifies
the attempt. Retrieval tracing is also optional and independent of
`[OperationalEvents] openTelemetryEnabled`: when enabled, one trace follows a
Master retrieval of a DataFile, including each remote Mover attempt and the
Mover-side download span. Retries across Movers appear in that same retrieval
trace. Each successful RET log for transfers related to the DataFile shares the
trace context and `ecpds.retrieval.id`; unsuccessful retrievals have error spans
but do not emit RET logs. Acquisition directory discovery is not covered by
retrieval traces. Actual portal uploads and downloads have a separate
Master-side `ecpds.portal.transfer` span; its recorded interval, byte count,
protocol and server come from the completed transfer's protocol event. The
associated OpenTelemetry INH log carries that span's trace context; multiple
INH records for one portal operation share its span. The `scheduled` INH action
records an `ecpds` scheduling request, not a file transfer, and is not traced.
These portal spans use the same Master tracing configuration and sampling.
Apply access controls and retention to tracing data as well as logs.

The default sampler is **parent-based trace-ID ratio sampling**: the Master
samples approximately 1% of attempts and Movers honour that decision. Set
`tracesSampleRatio=1.0` for all attempts, or `0.0` for none. Standard
`OTEL_TRACES_SAMPLER` / `OTEL_TRACES_SAMPLER_ARG` override these defaults; retain
a parent-based sampler.
`OTEL_BSP_MAX_QUEUE_SIZE`, `OTEL_BSP_MAX_EXPORT_BATCH_SIZE` and
`OTEL_BSP_SCHEDULE_DELAY` override the corresponding trace batching defaults,
with JVM `-Dotel.*` properties taking precedence over environment values.
The delays are in milliseconds. `OTEL_TRACES_EXPORTER` defaults to `otlp`;
`OTEL_SDK_DISABLED` also applies.

**Trace delivery is best effort and is not database-journalled.** Bounded
queues, Collector outages, crashes or abrupt shutdown can lose spans.
Export failures are reported in service logs. A linked log may have no
corresponding stored trace because of sampling, export failure or trace retention.
Log delivery and transfer success do not depend on trace delivery.

### OpenTelemetry metrics

Master application metrics are optional and independent of native logs and
traces. Enable them in `[OpenTelemetry]`:

```ini
[OpenTelemetry]
metricsEnabled=yes
metricsExportIntervalMillis=60000
metricsExportTimeoutMillis=30000
```

They use the same OTLP connection settings as logs and traces, but a separate
SDK/provider. Metrics do not replace the existing Prometheus/JMX JVM and MBean
export. The application instruments cover transfer dispatch/completion,
retrieval attempts/completion, and actual portal transfers, with counters and
duration/byte histograms. Transfer outcomes and portal direction use bounded
attributes; transfer, file, user, destination and host IDs are never metric
labels. The SDK can attach trace-based exemplars to recorded measurements when
the associated operation has a sampled trace. Counts and histograms are
recorded independently of trace sampling.

Observable gauges reuse Master-side sources behind the Live ECPDS Earth KPIs:
rolling 24-hour transferred bytes by Host Type, open Data Portal sessions and
rolling portal upload/download rates, and cached DataMover storage used and
capacity. The storage aggregation uses the same per-Mover cache snapshot as the
globe. Active transfer count, distinct-host and throughput gauges are emitted
only while live transfer sampling is enabled, normally by an interested globe
client. Their absence alone does not establish whether transfers are active.

The SDK exports OTLP metrics by default when enabled. Standard
`OTEL_METRIC_EXPORT_INTERVAL` and `OTEL_METRIC_EXPORT_TIMEOUT` (or corresponding
`otel.metric.export.*` JVM properties) override the interval and timeout in
`ecmwf.properties`. Metrics export is best effort and is not journalled.

#### Metric inventory

The following inventory covers the Master's application metrics. OTLP carries
the dotted instrument names in the first column. The second column shows their
Prometheus names with standard unit and counter-suffix translation enabled;
Collector/exporter translation settings can change these names.

Each histogram base name expands into three Prometheus series:
`<base>_bucket`, `<base>_count` and `<base>_sum`. Buckets also have an `le`
label. Durations are in milliseconds, sizes are in bytes and throughput is in
bytes per second.

| OTLP instrument | Prometheus name or histogram base | Type | Point attributes | Meaning |
|---|---|---|---|---|
| `ecpds.transfer.attempts` | `ecpds_transfer_attempts_total` | Counter | None | Transfer dispatch attempts. |
| `ecpds.transfer.completions` | `ecpds_transfer_completions_total` | Counter | `outcome` | Transfer completion/status events, including retries and failures. |
| `ecpds.transfer.duration` | `ecpds_transfer_duration_milliseconds` | Histogram | `outcome` | Duration reported at transfer completion. |
| `ecpds.transfer.bytes` | `ecpds_transfer_bytes` | Histogram | `outcome` | Bytes reported sent at transfer completion, including unsuccessful attempts. |
| `ecpds.retrieval.mover.attempts` | `ecpds_retrieval_mover_attempts_total` | Counter | `outcome` | Successful and failed Mover retrieval attempts. |
| `ecpds.retrieval.completions` | `ecpds_retrieval_completions_total` | Counter | `outcome` | Successful and failed retrieval completions. |
| `ecpds.retrieval.duration` | `ecpds_retrieval_duration_milliseconds` | Histogram | `outcome` | Retrieval completion duration. |
| `ecpds.retrieval.bytes` | `ecpds_retrieval_bytes` | Histogram | `outcome` | Retrieved bytes; recorded only for successful retrievals. |
| `ecpds.portal.transfers` | `ecpds_portal_transfers_total` | Counter | `direction`, `outcome` | Actual portal upload/download completions. |
| `ecpds.portal.transfer.duration` | `ecpds_portal_transfer_duration_milliseconds` | Histogram | `direction`, `outcome` | Portal transfer duration. |
| `ecpds.portal.transfer.bytes` | `ecpds_portal_transfer_bytes` | Histogram | `direction`, `outcome` | Portal transfer bytes, including unsuccessful transfers. |
| `ecpds.traffic.bytes.24h` | `ecpds_traffic_bytes_24h` | Gauge | `host.type` | Actual bytes transferred in the rolling 24-hour window; independent of live sampling. |
| `ecpds.portal.sessions` | `ecpds_portal_sessions` | Gauge | None | Currently open Data Portal connections. |
| `ecpds.portal.throughput` | `ecpds_portal_throughput_bytes_per_second` | Gauge | `direction` | Rolling portal upload/download throughput. |
| `ecpds.storage.bytes` | `ecpds_storage_bytes` | Gauge | `state` | Aggregate cached DataMover storage usage and capacity. |
| `ecpds.transfer.active` | `ecpds_transfer_active` | Gauge | `host.type` | Active sampled transfers. |
| `ecpds.transfer.active.hosts` | `ecpds_transfer_active_hosts` | Gauge | `host.type` | Distinct named hosts among active sampled transfers. |
| `ecpds.transfer.active.throughput` | `ecpds_transfer_active_throughput_bytes_per_second` | Gauge | `host.type` | Sum of positive, finite active sampled throughput. |

`outcome` is `success`, `retry` or `failure` for transfer completions and their
histograms; retrieval and portal instruments use `success` or `failure`.
`direction` is `upload` or `download`, and storage `state` is `used` or
`capacity`. `host.type` uses `Dissemination`, `Acquisition`, `Replication`,
`Source`, `Backup` or `Proxy`; Prometheus normally translates this attribute
key to `host_type`. Resource attributes such as `service.name` identify the
exporting Master separately; their mapping to Prometheus labels depends on
Collector/exporter configuration.

These are **18 instruments**, normally producing **30 distinct Prometheus
metric names** when all are populated: five counters, six histograms with
three names each, and seven gauges. Labels and histogram buckets create
additional time series, not additional instrument names. JVM/process metrics
are not registered by this component; configure separate instrumentation or
the existing Prometheus/JMX export for those.

#### Live sampling and missing metrics

To keep the three `ecpds.transfer.active*` gauges sampling without an open
globe page, add this to the Master's local `ecmwf.properties` override and
restart the Master:

```ini
[Server]
liveTransferMonitoringForceEnabled=yes
```

This keeps the underlying live transfer sampling active, not just metric
export. It is not required for completion counters/histograms, portal metrics,
storage or rolling 24-hour traffic accounting.

Even with sampling enabled, the active gauges do not emit explicit zero
points for empty Host Types. The distinct-host gauge needs a named host,
and the throughput gauge needs a positive, finite rate. Missing points can
therefore mean disabled sampling, no qualifying samples, or export/filtering
issues; they are not an authoritative zero.

Counters and histograms appear only after qualifying activity is recorded;
they are not pre-populated with zero series. Histograms skip negative
measurements, and retrieval byte measurements require success. Allow at least
one configured export interval after activity before checking the Collector.

Transfer completion counts, durations and bytes are recorded together in the
Master's completion handler. If fresh `ecpds_transfer_bytes_*` data arrives
but completion counts and non-negative durations remain absent, check the
deployed software version, Collector metric filters/transforms and exporter
name translation. Existing historical series alone do not prove that the
current Master is still exporting that instrument.

### Per-signal resource attributes

`OTEL_RESOURCE_ATTRIBUTES` remains the shared resource baseline. When the Collector
cannot be changed, ECPDS also supports these **ECPDS-specific extensions**:

```sh
export OTEL_RESOURCE_ATTRIBUTES="deployment.environment.name=production,site=ecmwf"
export ECPDS_OTEL_LOGS_RESOURCE_ATTRIBUTES="routing.dataset=ecpds-logs"
export ECPDS_OTEL_METRICS_RESOURCE_ATTRIBUTES="routing.dataset=ecpds-metrics"
export ECPDS_OTEL_TRACES_RESOURCE_ATTRIBUTES="routing.dataset=ecpds-traces"
```

Set these in the service environment (or export them from `/etc/ecpds/master.cnf`),
then restart the Master. No Collector change is needed. The equivalent JVM
properties are `-Decpds.otel.logs.resource.attributes=...`,
`-Decpds.otel.metrics.resource.attributes=...` and
`-Decpds.otel.traces.resource.attributes=...`; a JVM property takes precedence over
the corresponding environment variable. These settings are not read from
`ecmwf.properties`.

Values use the same comma-separated `key=value` syntax as `OTEL_RESOURCE_ATTRIBUTES`.
Each signal's values override matching shared keys and service defaults; other
keys remain unchanged. An unset or empty override preserves existing behavior.
These are resource attributes, not log-record, metric-point or span attributes.
Keep `service.name` and other service identity fields consistent unless intentionally
separating services, to preserve cross-signal correlation.

The Master's trace overrides also apply to Mover spans forwarded through it.
Forwarded spans otherwise retain their original Mover resource identity; they do
not inherit the Master's shared resource attributes. Already-journalled log records
retain their original resource attributes when retried.

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
  The top-level timestamp represents the absolute instant; the attribute uses
  the Master JVM's default timezone and has no zone suffix. Other date/time
  fields use their category's textual formatting and may likewise omit a zone.
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

**TLS certificate validation:** `[OpenTelemetry] tlsVerify=yes` is the secure
default. `tlsVerify=no` is an explicit, exporter-local opt-out for temporary testing,
covering OTLP logs, traces and metrics over HTTP/protobuf or gRPC.
It disables certificate trust/validity validation, emits a warning and leaves HTTPS
encryption and the Java sender's hostname verification in place. Like Fluent Bit,
certificate verification is distinct from hostname verification; this option does
not copy Fluent Bit's default of disabling the latter. It never changes JVM-wide
HTTPS defaults. Disabling checks permits server impersonation and exposure of
Bearer tokens and telemetry; prefer `OTEL_EXPORTER_OTLP_CERTIFICATE` with an approved CA.
Restart the service after changes. Invalid values, non-OTLP exporters with the
opt-out, and combining the opt-out with OTLP client TLS credentials fail startup.

The Master routes OpenTelemetry SDK diagnostics for logs, traces and metrics to
**`master.log`**, including HTTP status (such as 401), TLS and connection errors.
These JUL diagnostics do not propagate to the stderr/systemd console handler.
The dedicated `OpenTelemetrySdk` Log4j logger is file-only, even when general
console logging is enabled; its default threshold is WARN (`log.otel.level`).
SDK throttling applies. Other libraries' stderr output is not redirected.

Without database journaling, delivery uses a bounded in-memory queue and is
**best effort**. A full queue, export failure or process shutdown can lose
records; retries may also create duplicates. Check the Master service log and
Collector diagnostics for delivery errors. For stronger durability, enable the
[database journal](#durable-database-journal), enable the Splunk file and/or use
a local Collector with persistent queues, and monitor the pipeline's health.
The outputs are independent, not automatic failover:
OpenTelemetry failure does not turn file logging on if it was disabled.

### Durable database journal

For recovery during endpoint outages and Master restarts, enable the optional
database journal. It stores each native event **before** attempting delivery,
not only after an export has already failed. The `splunk.log` output remains
independent and is not replayed by this feature.

Journaling requires the `OPERATIONAL_EVENT` table in the Master's database,
included in the supplied installation schema. The running service does not
create tables. The Master uses the supplied SQL files to access the journal.

Then configure and restart the Master:

```ini
[OperationalEvents]
splunkEnabled=yes
openTelemetryEnabled=yes
journalEnabled=yes
journalOwner=production-master
journalMaxPendingEvents=10000
journalMaxPayloadBytes=67108864
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

Persisted events are retained until export is acknowledged and are retried
after failures or Master restarts. Retries can produce duplicates; use
`ecpds.event.id` for downstream deduplication.

In journal mode, event producers serialize and enqueue without database I/O.
A single background worker admits events to the database before export.
The memory admission queue is bounded to 2,048 events and 8 MiB of UTF-8 payload;
oversized events and new arrivals when it is full are dropped without waiting.
Memory-queued events can be lost during shutdown or a crash; `forceFlush` does not
provide a database-persistence barrier. Shutdown need not drain the database;
the next start resumes persisted work. `OTEL_BLRP_*` does not control journal admission.
Disabling OpenTelemetry or journaling pauses replay without deleting pending
rows. Re-enabling it with the same owner resumes replay.

#### Guarantees and operational limits

- **Endpoint outages:** successfully persisted, unacknowledged events have no
  automatic expiry or retry limit, including permanent HTTP errors. Fix the
  endpoint or credentials; an indefinitely rejected batch can block the backlog.
  An undecodable row is also retained with an error for operator investigation.
- **Database failures:** operations do not wait for database admission. New telemetry
  is dropped when persistence fails or capacity cannot be established; there is no
  alternate exporter bypass. A rate-limited `DATABASE EVENT JOURNAL UNAVAILABLE`
  error reports failed admissions. An ambiguous insert response may leave a row
  persisted even though the admission was counted as dropped/unconfirmed.
- **Acknowledgements:** an OTLP acknowledgement is not proof of indexing or
  durable storage in the final platform. Partially rejected batches are not
  automatically replayed. Monitor Collector/backend rejection diagnostics.
- **Coverage:** the journal protects successfully persisted events; it is not
  a transactional audit trail and cannot guarantee an event for every operation.
- **Capacity:** `journalMaxPendingEvents` (default 10,000) and
  `journalMaxPayloadBytes` (default 67,108,864 = 64 MiB) limit new admissions
  **per owner**, under the single-active-writer requirement above. Limits require
  a Master restart. At either limit the worker drops **new** events and retains
  queued events for retry; it never purges existing rows. It rechecks capacity
  in the background at most once per minute, scanning at most the row limit plus one.
  An oversized existing backlog is reported as unavailable rather than an exact
  count, and new admission remains blocked until capacity can be established.
  Existing data above a limit is not reduced automatically.
- **Accounting and storage:** after a successful capacity snapshot, the sole worker
  accounts for its own inserts and acknowledged deletions. Ambiguous writes/deletes
  suspend admission until remeasurement. Do not write/delete this owner's rows
  from another process while it is active. These are serialized UTF-8 payload
  limits, **not physical table, heap or cluster disk limits**: indexes, row overhead,
  binary logs, Galera copies, other owners and legacy rows need additional capacity.
  No new accounting table or database schema change is required.
- **Drops:** the Monitor displays dropped/unconfirmed admissions since startup and
  the last reason; the Master logs rate-limited `TELEMETRY DROPPED` warnings.
  Counts reset on restart. Dropped events are not automatically replayed or backfilled;
  Splunk/file output is independent and unchanged.
- **Storage protection:** database durability, replication and backups remain
  essential. Protect journal payloads using the same access and retention
  controls as operational logs. Successfully acknowledged events are deleted;
  this table is not a permanent history archive.

Monitor pending count and oldest admission time through
**Admin Tasks → Operational Event Journal**.
For a local Docker Collector that prints received records, use
`make start-otel` and `make logs-otel`; an optional Eclipse launch group starts
it before debugging the Master. See
[Local OpenTelemetry and journal testing](../getting-started/ide-setup.md#local-opentelemetry-and-journal-testing).

Alert on growing backlog age/count, journal/export error messages, database disk
space and rejected records downstream. Do not routinely delete unacknowledged
rows: doing so intentionally discards events. A local Collector with persistent
queues remains useful to protect the next hop after the journal receives an
OTLP acknowledgement.

The Monitor page shows pending count, oldest admission timestamp and age,
in-flight records, last successful persistence, last endpoint acknowledgement,
and persistence/delivery failure history, admission limits and dropped events.
It reads cached worker statistics, at most one minute old; UI refresh does not run
a full-table aggregate. The page refreshes every 30 seconds by
default, with manual refresh and an option to disable automatic refresh. Warning
and critical thresholds are configurable with `journalWarningPendingCount`,
`journalCriticalPendingCount`, `journalWarningOldestAgeMillis` and
`journalCriticalOldestAgeMillis` in `[OperationalEvents]`; changes require a
Master restart. Both count and oldest-age thresholds can trigger an alert.

Administrators can pause or resume native event collection independently from
delivery. Pausing collection does not change the `splunk.log` output and does
not stop delivery of already-journalled events. Pausing delivery continues
database admission, so the backlog can grow; a batch already in flight may
complete. The runtime controls are audited and reset when the Master restarts.
The page displays a browser-local backlog trend. It estimates drain time only
after at least four queue samples spanning two minutes show a healthy decrease
of at least two events; treat the estimate as an extrapolation, not a guarantee.

Persistence failures highlight the Admin Tasks card, home-page and administration
menu entries, with a banner on the home and Admin Tasks pages. Red means persistence
has failed without a successful admission confirming recovery, or the worker is
stopped. After persistence recovers, a yellow warning retains the failure count
for the rest of the Master process. Runtime counters reset on Master restart;
persisted pending rows do not. Export failures also produce an attention indicator.
Detailed exceptions remain in service logs; payloads and credentials are not shown.

The task is greyed out when journaling is disabled on the Master.
Disabled journaling can still leave pending rows in the database.
A Master connection failure is shown as unavailable. The page is read-only
for event data: it does not expose payloads, purge records or force retries.

The publisher exports the seven categories listed below. Operational events
and the Monitor's database change log are separate records.

## Event Categories

OpenECPDS generates various types of events, each categorised based on the nature of the
operation being recorded:

| Code | Category | Description | Fields |
|------|----------|-------------|--------|
| **PRS** | PRoduct Status | Tracks the status of products as they progress through OpenECPDS. The system maintains a top-level monitoring display, which can be updated using the `ecpds` command. Events are logged at key stages, including when a set of files is scheduled, when files start being registered, and when they are fully recorded. | [PRS fields](prs-fields.md) |
| **RET** | RETrieval | Records a successful retrieval by the Retrieval Scheduler. Failed retrievals do not emit RET events; when tracing is enabled, their failed Mover attempts are visible in retrieval traces. | [RET fields](ret-fields.md) |
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
    All duration values are measured in **milliseconds**. Timestamps in the legacy Splunk
    output and timestamp-valued attributes are formatted in the Master JVM's default
    timezone, without an explicit offset. All timestamps are formatted as
    `yyyy-MM-dd HH:mm:ss.fffffffff`, where `yyyy` is the 4-digit year, `MM` is the
    2-digit month, `dd` is the 2-digit day, `HH` is the 2-digit hour (24-hour clock),
    `mm` is minutes, `ss` is seconds, and `fffffffff` represents fractional seconds in
    nanoseconds. OpenTelemetry's top-level log timestamp is an absolute instant; use it
    for timezone-independent correlation. The examples assume the Master JVM uses UTC.

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
