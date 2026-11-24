# Notification history and replay

Notification history is a native OpenECPDS service enabled globally by default,
with Aviso opt-in per destination. It records file
availability events in MariaDB independently of MQTT and exposes them to Data
Portal users through a Data Mover's HTTPS endpoint. MQTT topics, payloads and WIS2
publications are configured independently of this service.

## Enable history

The standard database schema includes the notification tables and transfer
revision column and trigger.
The Master's default configuration is:

```ini
[NotificationHistory]
enabled=yes
revisionFencing=yes
retention=7d
maxPayloadBytes=1073741824
maxOutboxBytes=1073741824
mqttTopicPrefix=open-ecpds/notifications
```

Set both `enabled=no` and `revisionFencing=no` to disable the service, including
native history and durable MQTT CloudEvents. Destinations default to
`notification.aviso=no`. Enable Aviso
for a destination in its options:

```ini
incoming.event="yes"
notification.aviso="yes"
```

For acquisition, use the existing Host `acquisition.event=yes` trigger instead
of the destination's `incoming.event`. A transfer must have its event trigger
enabled as well as destination Aviso opt-in to generate Aviso history.

The default retention is seven days, and the default cap is 1 GiB of UTF-8 event
payloads. At the cap, the oldest events are removed first, potentially shortening
the replay window.
Indexes, table overhead and cluster replicas use additional database space.
Expired events are unavailable to replay even if background deletion has not
yet removed them from disk. History operations contend over a shared database
row; Galera can abort conflicting writes, which are retried a limited number
of times. Measure throughput and database contention for your load.
The Aviso API does not expose server-managed subscription checkpoints.
The optional [Aviso acquisition receiver](aviso-acquisition.md) maintains its own
durable acquisition progress on the receiving Master.

Events use the transfer notification trigger: `incoming.event` or
`acquisition.event` must be enabled. The Master
must also have its history and event repositories enabled. History does not
require MQTT publishing to be enabled.

When both `notification.aviso` and `mqtt.cloudEvents` are off, the native
notification event is not built or queued and no history/outbox write is made
for that transfer. Existing scripted notifications and ordinary MQTT publishing
are unaffected. `mqtt.cloudEvents=yes` remains independent of Aviso opt-in and
retains the shared history/outbox path needed for durable delivery and strict
capture idempotency. Such history is not accessible through Aviso until the
destination has `notification.aviso=yes`.

Global background cleanup and outbox polling still run when the service is
enabled, even if no destination opts in. Active watch/replay requests add
database reads.

Capture is asynchronous by default. A process failure, queue
loss or database error before persistence can lose a notification without
blocking the transfer. Only successfully stored events can be replayed. A
bounded in-memory writer queue holds up to 4,096 events; full-queue and
persistence failures are logged and do not retry indefinitely. A
stored notification is not proof that a subscriber received or processed it.
Native event details are captured when the notification is queued, rather than
read later from a transfer that may have changed.
Capture is not atomic with the originating transfer update, and the service
may lose events before they are persisted. The read-only Aviso API exposes
persisted records only; it does not publish or accept notifications.

## Strict capture

Capture is asynchronous by default. Set `notification.captureMode="strict"` on
a destination to require the transfer update and native notification history/outbox
to commit in one transaction before the originating Master acknowledges the update.
The global settings supporting strict capture are enabled by default:

```ini
[NotificationHistory]
enabled=yes
revisionFencing=yes
```

Revision fencing is validated at Master startup; an absent or incompatible
revision column/trigger prevents startup. Revision fencing must be enabled on the
active Master when strict destinations are enabled, including after failover.
The default `async` mode is nonblocking.
An explicit `revisionFencing=no` disables fencing. Asynchronous capture still
works, but a strict capture request fails with
`Strict notification capture requires NotificationHistory, revisionFencing, and EventRepository`.
This is an operation error, not a dedicated Monitor warning or a silent fallback
to asynchronous capture.
Strict mode requires the transfer event trigger (`incoming.event` or
`acquisition.event`), notification repositories, and either
`notification.aviso=yes` or `mqtt.cloudEvents=yes`. Destinations with neither
opt-in do not enter strict native capture. It propagates
capture failures to the originating operation rather than acknowledging the
transfer; a retry of the same captured revision is recognized by its stable
event identity and does not create a second history/outbox record.

Strict capture also covers completed Mover transfers and the Master/portal
standby completion paths. Ordinary database writers advance the revision via
the trigger, so stale completions cannot overwrite a newer transfer state.
`mqtt.cloudEvents=yes` remains an independent choice to create durable MQTT
deliveries; strict history capture does not require MQTT.

Each event is structured CloudEvents JSON with a unique ID, source, type
`org.openecpds.file.available.v1`, time, destination and transfer details.
Consumers can use `(source, id)` to identify a stored event; a repeated
transfer notification can produce a separate event.

## MQTT CloudEvents

Set `mqtt.cloudEvents=yes` in a destination's options to publish its captured
events on the separate topic `open-ecpds/notifications/<destination>`. The
destination is URL-encoded as a single topic level; `mqttTopicPrefix` changes
the prefix. Destination MQTT and WIS2 publications are independent.

History and pending MQTT deliveries are stored in one database transaction.
Every configured Data Mover at capture time receives an independent pending
delivery. The Master retries until each Mover confirms broker acceptance, using
QoS 1 and the same CloudEvent ID and payload on retry. Broker acceptance does
not mean subscribers received or processed the event; uncertain outcomes can
cause duplicates. The asynchronous capture limitations above still apply.
Each Master uses four publisher workers and normally makes one concurrent call
per Mover. A stalled Mover call does not occupy all workers, but four stalled
Movers can delay other deliveries. Calls and retries do not guarantee
publication order. Keep database server clocks synchronized: delivery leases
and retry times use database time.

`maxOutboxBytes` defaults to 1 GiB of UTF-8 payloads, excluding target rows,
indexes and replicas. Pending deliveries are not automatically discarded.
Unavailable or removed Movers can fill the outbox; restore their broker
delivery to drain it. When full, capture of MQTT-enabled events fails and is
logged, rather than silently deleting pending deliveries. A destination with
no configured Movers cannot capture MQTT-enabled events.

Use the Master service logs to investigate delivery failures. Do not edit pending
deliveries directly in the database.

For a removed Mover, restore a working Mover with the same configured name to
deliver its pending events. There is no automatic discard or management
operation for permanently removed targets.

**MQTT access is controlled by the Data Portal user's `portal.mqttPermission`
topic ACL, not by replay's per-file path permissions.** A subscriber allowed
to read a destination topic may see events for paths it cannot replay. Grant
topic access only to users authorized for all events published on that topic.

## Aviso client API

For an upload, watch and replay walkthrough using the standalone `aviso_demo`
destination, see [Try Aviso watch and replay](../getting-started/standalone.md#try-aviso-watch-and-replay).

The Mover exposes a read-only Aviso-compatible API under
`https://mover.example/ecpds/aviso/api/v1`. It supports the generic
`file_available` event only; notification publishing and management endpoints
are not provided.

Configure the official Aviso client's server base URL as
`https://mover.example/ecpds/aviso/`, for example with `AVISO_BASE_URL`.
The client appends `api/v1/...`; do not include that suffix in its base URL.

Schema discovery is public:

```sh
curl 'https://mover.example/ecpds/aviso/api/v1/schema'
curl 'https://mover.example/ecpds/aviso/api/v1/schema/file_available'
```

Watch/replay additionally require `notification.aviso=yes` for the requested
destination; otherwise an authorized user receives HTTP 403. Schema discovery
remains public regardless of that option.

Use the Data Portal credentials of a user with an active associated destination.
Each event is checked against the user's `get` permission and path restrictions;
inaccessible targets are omitted. The user's default destination and destination
root are also enforced. The Mover requires direct Master RMI connectivity.

`identifier.destination` is required. `identifier.target` is optional and, when
provided, matches the stored target path exactly. A watch without a cursor starts
with new events only:

```sh
curl -N --user "$DATA_USER" \
  -H 'Content-Type: application/json' \
  -d '{"event_type":"file_available","identifier":{"destination":"DEST"}}' \
  'https://mover.example/ecpds/aviso/api/v1/watch'
```

To replay from an event ID and then continue live, provide an inclusive
`from_id` to `watch`:

```json
{
  "event_type": "file_available",
  "identifier": {"destination": "DEST"},
  "from_id": "file_available@123"
}
```

`POST /aviso/api/v1/replay` requires `from_id` or `from_date` and closes after replay.
Both endpoints accept `from_id` or `from_date`, but not both. Replay also accepts
an inclusive `to_id` or `to_date`, but not both; `watch` does not accept an end
cursor. IDs have the form `file_available@<sequence>`. Date cursors are ISO
8601 timestamps and use the time at which history was stored.

The SSE stream uses Aviso frame names: `replay-control`, `replay`,
`live-notification`, `heartbeat`, `error` and `connection-closing`. Notification
frames contain Aviso CloudEvents (`type: int.ecmwf.aviso.file_available`) with
globally unique sequence IDs, the Mover API URL as `source`, and
`data.identifier` containing the destination and target. Transfer details are
carried in `data.payload`.

Sequences are global and monotonic; gaps between IDs are expected because a
destination sees only its own events and path permissions may hide events.
An initial cursor that has expired returns HTTP 410. If retention removes unread
events while streaming, the stream sends an Aviso `error` frame and closes.
Clients should persist an ID only after successfully processing its event;
reconnects can repeat events, and SSE receipt is not an acknowledgement.

The Mover's `[NotificationHistorySSE]` settings default to 16 simultaneous
streams, a two-second database polling interval and a five-minute connection
duration. Each active stream uses a request thread and queries history; size
these limits for the Mover and database. Authorization is checked on each
replay page. The duration limit is checked between frames and database reads;
a blocked write or Master call can delay closure. History retention and capture
limitations still apply.
