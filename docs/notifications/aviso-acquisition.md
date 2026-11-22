# One-way destination replication with Aviso

An HTTP acquisition Host can replicate files from a remote OpenECPDS destination
without an MQTT broker or a Python process. The receiving Master owns the durable
progress; Movers perform bounded inventory/replay discovery and ordinary HTTPS
retrieval. This is additive, one-way replication, not bidirectional synchronization
or deletion propagation. Do not configure replication cycles.

The receiving Master stores acquisition progress in the
`ACQUISITION_CHECKPOINT` database table.

## Source destination

Enable the remote destination:

```properties
notification.aviso = "yes"
notification.captureMode = "strict"
incoming.event = "yes"
```

For files acquired at the source, enable its acquisition Host's
`acquisition.event` instead of relying on `incoming.event`. The source Master's
notification history, revision fencing and event repository must be enabled.
See [notification history](history.md).

Use a dedicated **read-only Data Portal account** with list/get access restricted
to the source destination and required paths. Configure its credentials on the
receiving HTTP Host; never put credentials in the URL. The source endpoint must
be a directly connected, authenticated HTTPS Mover. Use a certificate trusted
by the receiver and enable `http.sslValidation`.

Aviso history and cursor control requests can be relayed via a delegate Mover
when `[Security] rccSharedSecret` is configured on every hop. This relay does
not add Data Portal authentication or file access to REST-only Movers, so it
does not replace the directly connected source endpoint requirement above.

## Receiving acquisition Host

For a remote destination `REMOTE`, configure the Host address as `source.example`,
its Data Portal login/password, and its Directory as `/REMOTE/`. If that account
has `REMOTE` as its default destination, use `/` instead. This is the destination's
portal root, not an arbitrary subdirectory. A date-grouped source is supported;
its original logical target paths, rather than its date-directory presentation,
are retained.

```properties
http.scheme = "https"
http.port = "443"
http.sslValidation = "yes"
http.strict = "yes"
http.ftpLike = "yes"
http.mqttMode = "no"
http.avisoMode = "yes"
http.avisoUrl = "https://source.example/ecpds/aviso/"
http.avisoDestination = "REMOTE"
http.avisoSourceId = "source-installation"
http.avisoReconcile = "1d"
http.avisoMaxEvents = "1000"

acquisition.useSymlink = "yes"
acquisition.action = "queue"
acquisition.noretrieval = "no"
acquisition.deleteoriginal = "no"
acquisition.target = "$target"
```

For a test endpoint with an untrusted certificate, `http.sslValidation = "no"`
disables certificate chain validation. This is insecure: an attacker can
impersonate the source and intercept credentials or alter files. Prefer installing
the source certificate in the receiver's trust store. `http.strict = "yes"`
still verifies the hostname and remains required. In Aviso mode, certificate
validation defaults to enabled when `http.sslValidation` is not configured;
an explicit Host or global setting is honored.

Aviso selects the `Accept` header for each API request; a custom
`http.headers` entry is not needed for it. HTTP content compression can remain
enabled. File size checks request identity encoding so the reported size refers
to the original file, while discovery and downloads can use compression.

The Host Properties editor reports missing or conflicting Aviso prerequisites on
both the view and edit pages, with the same errors reflected in Host badges and
the Properties error filter. Checks use configured global defaults and built-in
defaults; safe default values need not be repeated explicitly. The dependency
checks flag script- or placeholder-driven values as requiring runtime verification.
These diagnostics do not prevent saving the Host.

Keep the normal acquisition schedule enabled. Each discovery is a finite replay,
not a permanently open watch: subsequent cycles consume remaining/new events.
The existing retrieval queue performs the downloads independently of discovery.
Aviso processing on the Master is serial even if parallel listing processing is
configured. Nested names and spaces within names are supported; unsafe paths,
control characters, leading/trailing whitespace and ambiguous ` -> ` names fail
discovery rather than being renamed silently.

The usual acquisition target templates and size/age/name filters still apply.
Changing selection settings creates a new subscription checkpoint where necessary;
periodic reconciliation also rechecks the current inventory. Keep the Directory
and logical source identity stable. Internal `http.avisoCursor` and
`http.avisoInventoryAt` options are supplied by the Master, not by the operator.

## Recovery and guarantees

On first use, the receiver captures an authorized source sequence **before**
walking its recursive inventory. It queues that inventory, then replays events
after the watermark, covering arrivals during the scan. Normal cycles replay from
the durable cursor. A complete inventory runs on the next cycle after
`avisoReconcile` elapses;
HTTP 410 history expiry triggers an immediate inventory and a visible warning.
This recovers the current files, not historical versions already removed.

Inventory JSON and event payloads include `dataFileId`,
`fileInstance` (null instances represented as zero), and `etag`.
Events also capture the portal download path. Version metadata is captured at
publication, not looked up when an old event is replayed. Deduplication uses the
logical source, remote destination, original target and ETag, independent of event
timestamp or selected Mover URL. Reupload/requeue versions are distinct; an ETag
is a version identifier, **not a content checksum**.

Downloads carry `?etag=<version>` and check the actual opened DataFile generation
before committing HTTP headers. `If-Match` is also supported. Version-pinned
downloads never refill missing bytes from an unversioned upstream original.
A superseded or unavailable version fails instead of substituting newer bytes.
An already opened old-version stream may finish normally.

The Master acknowledges a complete discovery only after all selected entries
have been durably queued or recognized as duplicates, or deliberately excluded
by selection filters. Malformed metadata, failed registration, interruption and
incomplete replay leave the checkpoint unchanged. A crash after queueing but
before acknowledgment causes duplicate-safe replay. Compare-and-set checkpoint
updates prevent stale discovery results from rolling progress backward.
Acknowledgment means **queued**, not successfully downloaded.

`listMaxFiles` and `listMaxDirs` are hard inventory limits. Exceeding them fails
the cycle without advancing progress; it does not truncate inventory. Each JSON
directory response is limited to 32 MiB and each SSE frame to 1 MiB. Increase the
normal acquisition duration for inventories requiring more time.

The source history and stored bytes must outlive the receiver's discovery and
retrieval delays. Notifications do not pin bytes. Reconciliation cannot recover
an overwritten intermediate version or a file deleted at the source before it
was retrieved. Local retention and normal acquisition retry policies still apply.

Use the same `avisoSourceId` across source Mover failover. If the source sequence
moves backward after a database restore/reset, discovery fails visibly; use a new
logical source ID to bootstrap that replacement installation. Always change the
identity after a source database reset that can reuse file IDs, even if its
sequence has already caught up and the backward move was not observed.

## Aviso cursor formats

The `from_id` and `to_id` fields accept positive decimal strings (for example
`"41"`), positive JSON integers, and prefixed cursors such as `"file_available@41"`.
Emitted CloudEvent IDs use `file_available@<sequence>`. Invalid, negative,
zero, fractional and overflowing cursor values are rejected.

Event payloads include the file-version fields.
`replay_completed.last_sequence` reports the final scanned
position, including sequence gaps. The authenticated
`GET /ecpds/aviso/api/v1/cursor/{destination}` endpoint supplies the bootstrap
watermark; it does not create a remote subscription checkpoint.
