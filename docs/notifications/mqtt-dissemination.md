# Real-Time Data Dissemination with MQTT Broker

The MQTT Broker enables users of the dissemination service to subscribe to notifications,
ensuring they are alerted as soon as new products become available in the Data Store.

## Functional overview of the Notification System

The Notification System involves several key components:

- The **Product Data Store**.
- The **Dissemination and Acquisition Systems**.
- The **MQTT and Message Brokers**.

Below the Data Store, **OpenECPDS Destinations** serve to organise and manage products for
each customer. These [destinations](../concepts/entities.md#destinations-and-aliases)
include various configuration parameters, including an **MQTT topic** used for message
identification and routing to the appropriate MQTT subscribers.

A **notification** is triggered each time a product is added to the Data Store via the
Acquisition System. The product may either be made available through the Portal or pushed
to a remote site by the Dissemination System. In both cases, the user receiving the
notification will extract the product's location from the message:

- If the product is on the **Portal**, the user must retrieve it before processing.
- If it has been **pushed to a remote site**, it is immediately available for processing.

## Number of notifications

The number of notifications triggered when a new product is added depends on the
**destination's configuration**:

- **No notification** is sent if the destination is not configured to report updates.
- **One or two notifications** are sent if the destination is configured to notify one or
  both Brokers.

If the **MQTT Broker** is notified, it checks whether any MQTT clients have subscribed to
the destination's topic. If a matching subscription exists, the client receives the
notification along with the associated payload.

For the **Message Broker**, the process is simpler: upon receiving a notification, it
directly forwards it to the configured clients.

## Typical interaction in the OpenECPDS Notification System

The MQTT-based notification system follows a structured interaction between three key
components: the **MQTT Client**, the **MQTT Broker**, and the **OpenECPDS Data Store**.

- **Connection Establishment** — The process begins with the Client initiating a
  connection to the Broker by sending a **CONNECT** message. The Broker acknowledges the
  connection by responding with a **CONNACK** message.
- **Subscription to a Topic** — Once connected, the Client registers its interest in a
  specific topic by sending a **SUBSCRIBE** message to the Broker. The Broker then listens
  for relevant **PUBLISH** messages from the Data Store that match the subscribed topic.
- **Message Delivery and Product Retrieval** — When the Data Store generates a **PUBLISH**
  message for a subscribed topic, the Broker forwards it to all Clients that have
  subscribed to that topic. The Client then extracts the file location from the message
  and proceeds to fetch the product using one of the available protocols on the OpenECPDS
  Portal: **HTTPS, S3, WebDAV, SFTP, or FTP**.

## Retained messages and late client connections

When the **retain flag** is enabled in the MQTT message, even if the Data Store sends a
**PUBLISH** message before the Client is connected, the message remains available for
delivery. As a result, when the Client connects later, it still receives the retained
message, ensuring no critical notifications are missed. See
[Retained Messages in MQTT](mqtt-overview.md#retained-messages-in-mqtt).

## Configuration and access control

The MQTT configuration can be fine-tuned at the **destination level**, allowing for
greater flexibility in how notifications are handled. Additionally, **access control**
mechanisms can be configured at the **data user** level, ensuring secure and controlled
distribution of messages.

## Configuring a destination to publish MQTT notifications

### Step 1 — Enable event triggering and MQTT publishing

Add the following to the destination's **Properties** field. The `incoming.event` (for
portal uploads/downloads) or `acquisition.event` (for acquired files) flag is what causes
a publication to be queued; `mqtt.publish` then routes that publication to the MQTT broker.

```properties
incoming.event   = "yes"   # or acquisition.event = "yes" for acquired data
mqtt.publish     = "yes"
mqtt.contentType = "application/json"
mqtt.expiryInterval = "PT48H"
mqtt.retain      = "yes"
```

By default the topic is `destinationName/targetFileName`. Override it with `mqtt.topic`:

```properties
mqtt.topic = "my/custom/topic/"   # trailing slash -> target name is appended
```

### Step 2 — Build a rich JSON payload with JavaScript

For static payloads use `mqtt.payload` directly in Properties. For dynamic payloads that
embed file metadata and access URLs, place a JavaScript snippet in the destination's
**JavaScript** field. The script is executed for every completed transfer and can override
any `mqtt.*` option by returning a plain object.

See the [full reference and example](../concepts/destination-options.md#building-the-payload-with-javascript)
in the Destination Options guide, including a complete WIS2-style GeoJSON notification
with HTTPS, SFTP, WebDAV and S3 download links.

### Demo upload notifications and public download links

The standalone database seeds **mqtt_demo**, accessible to the `test` Data Portal
user through `test_destinations`. Upload a file into `/mqtt_demo/` to generate a
JSON notification with its identity, destination, relative filename, size,
publication/expiry timestamps and HTTPS, SFTP and S3 download links.

The upload-only demo sets `scheduler.standby = "yes"`: uploaded transfers enter
`HOLD` (available for download), which triggers the event without outbound delivery.
Without standby, an upload enters `WAIT` and normally notifies only after outbound
delivery completes; a stopped destination leaves it waiting without a publication.
Keep the demo destination stopped to avoid scheduling outbound delivery.

The destination's JavaScript starts with four editable settings:

```javascript
var httpsBase = "https://localhost:7443";
var sftpBase = "sftp://localhost:7022";
var s3Endpoint = "https://localhost:7443/s3";
var s3Bucket = "test";
```

These are standalone defaults. Eclipse uses HTTPS **4443** and SFTP **4022**.
Replace `localhost` with a subscriber-reachable public hostname for remote clients.
Base URLs have no trailing slash; `httpsBase` is the portal origin, not a listing URL.
For example, `sample file.dat` uploaded into `mqtt_demo` produces:

```text
https://localhost:7443/ecpds/data/file/mqtt_demo/sample%20file.dat
sftp://localhost:7022/mqtt_demo/sample%20file.dat
https://localhost:7443/s3/test/mqtt_demo/sample%20file.dat
```

Links contain no passwords or access tokens and do not bypass authorization.
HTTPS/SFTP require a Data Portal account with access to the destination. S3 uses
the configured user's bucket (`test` in this demo) and normally requires a signed
request, not a browser click. The S3 link also supplies `endpoint`, `bucket` and
the unencoded object `key` for SDK clients. Use the same `key` under another
user's bucket only if that user has destination access. Clients must trust the
portal TLS certificate. URLs remain subject to file expiry and service availability.

**Why configure public endpoints explicitly?** Publication runs on the Master,
without a browser request. Mover names, internal addresses and upload connections
do not reliably identify a public endpoint behind NAT, proxies or load balancers.
Use the same public values as the portal's `[DataPortal]`
`httpsPublicBaseUrl`, `s3PublicEndpointUrl`, `sftpPublicHost` and `sftpPublicPort`
hints. These Mover-local hints are not currently exposed to MQTT scripts
automatically. In multi-Mover deployments advertise a stable public service
that can retrieve the file, rather than assuming any broker's address is a download URL.

The demo reads **`mqttEventJson`**, a JSON string supplied as a script binding
(not interpolated into script source), with fields `id`, `filename`, `destination`,
`size`, `dataFileId`, `transferId`, `publishedAt` and `expiresAt`.
IDs are strings except `size`; timestamps are UTC ISO-8601.
The target filename is relative to the destination; the script adds the destination
directory and URL-encodes each segment. This preserves spaces, Unicode and literal
percent signs without treating quotes in filenames as JavaScript. Existing
`$`-placeholder scripts remain supported. This binding requires updated Master
code; database seed edits do not update existing destinations automatically.

#### Inline content for small files

The demo also sets `mqtt.inlineMaxBytes = "16384"` in Properties. It is disabled
by default for other destinations (`0`); the Master always caps it at **16 KiB
of original bytes**, even if a larger value is configured. The Master reads at
most the limit plus one byte before running JavaScript. Files above the limit,
unknown sizes, size mismatches and read failures omit inline content without
failing the notification. Download links are always retained.

When the complete file is available, `mqttEventJson` includes `contentBase64`.
The demo JavaScript adds it to the message as:

```javascript
content: file.contentBase64 === undefined ? undefined : {
  encoding: "base64",
  type: "application/octet-stream",
  data: file.contentBase64
}
```

`content` is a top-level payload field, omitted by `JSON.stringify` when unavailable.
Base64 preserves arbitrary text and binary bytes without interpreting them as
JavaScript or assuming UTF-8. An empty file has an empty `data` string. Subscribers
decode Base64 to obtain the original bytes; encoded data is about one-third larger.
This is **encoding, not encryption**: all subscribers authorized for the topic can
read the embedded file without subsequently authenticating to the portal. Enable
this only on appropriately restricted topics. The activity dashboard/journal never
stores message bodies or embedded content.

### Step 3 — Grant subscribe access to a data user

Set `portal.mqttPermission` on the data user's account (in their **Properties** field):

```properties
portal.mqttPermission = "#"           # all topics
# or restrict to a subtree:
portal.mqttPermission = "hourly_aq/#"
```

## Testing MQTT notifications

After an MQTT publication finishes, the transfer history records its publication
ID and counts of broker-accepted, failed and unconfirmed Mover results. Acceptance
does not confirm subscriber delivery. This informational entry leaves the transfer
status unchanged and contains no payload; see **Operations > MQTT Activity** for
per-Mover details. RPC timeouts are unconfirmed, not successful.

The Master also emits an [`NTF` operational event](../event-logging/overview.md#mqtt-notification-outcomes-ntf)
per Mover outcome, through the shared Splunk/OpenTelemetry pipeline when enabled.
It includes correlation IDs, topic, QoS, retain flag, payload byte count and status,
but never payload content. No configured Movers produces one failed event with an
empty Mover name.

Once a destination is configured, test the subscription with
[mqttx](https://mqttx.app/cli) (recommended — supports `--insecure` without a CA file):

```bash
mqttx sub \
  -h localhost -p 8883 \
  -l mqtts \
  -u test -P test2021 \
  --insecure \
  -t '#'
```

Or with [Mosquitto](https://mosquitto.org/download/) (requires extracting the cert first):

```bash
# Fetch the broker's self-signed certificate directly
openssl s_client -connect localhost:8883 </dev/null 2>/dev/null \
  | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/ecpds.pem

mosquitto_sub \
  --host localhost --port 8883 \
  --username test --pw test2021 \
  --cafile /tmp/ecpds.pem \
  --insecure \
  --topic '#' -v
```

!!! note
    Port `8883` is MQTTS (MQTT over TLS). The `--insecure` flag skips hostname
    verification for self-signed certificates. In production, use a properly signed
    certificate and remove `--insecure`.

## Monitoring publication activity

Open **Operations → MQTT Activity** in the Monitor to follow destination event
publications. The dashboard refreshes every five seconds and provides activity and
outcome charts, destination/topic/status filters, pagination, and publication details
with a result for each Mover. **Pause updates** freezes automatic refreshes.

The Master records the result returned by each Mover after the MQTT broker's
asynchronous publish operation completes:

| Outcome | Meaning |
|---|---|
| Queued / Processing | The Master has observed the publication waiting or being processed |
| Broker accepted | Every recorded Mover broker accepted its publish operation |
| Mixed results | Movers returned different outcomes; inspect the per-Mover details |
| Failed | Publishing could not be attempted or the broker rejected it |
| Unconfirmed | A timeout, RPC failure or interruption prevents confirmation; the message may still have been accepted |
| Skipped | The topic/payload was empty, or the transfer was missing or expired |

Broker acceptance does **not** confirm subscriber receipt, application processing or
data download, regardless of QoS. The Mover waits up to ten seconds for broker
completion; a timeout does not cancel the underlying publication or cause an automatic
retry. Payload-script errors retain the existing publication scheduler's retry behavior.
An interrupted publication is marked unconfirmed after a Master restart, until the
scheduler processes it again.

This view covers outgoing **destination event publications**, not all MQTT client
traffic or subscriptions.

History is independent of the OpenTelemetry Journal and its telemetry admission
limits. The Master keeps a bounded in-memory cache backed by the `MQTT_ACTIVITY`
database table. `[MQTTActivity] maxEntries`, `maxBytes` and `retentionHours` configure
its bounds (defaults: 2,000 entries, 8 MiB serialized metadata, 24 hours since last
update); changes require a Master restart. See
[MQTT Activity configuration](../configuration/master.md#mqttactivity).
A single background worker coalesces updates,
persists changed entries and removes history outside those bounds. Retention
cleanup runs while the activity service is active. The regular database purge
also deletes expired entries across all Masters using each row's stored expiry,
including inactive Masters. The dashboard's **All retained history** time window
covers the configured retention.
Charts and totals describe the latest
known outcome of retained publications, not a complete historical counter or audit log.

`[MQTTActivity] owner` identifies the logical Master in this table. It defaults to
`[Login] root`, then `[Login] hostName` (finally `master`). Keep it stable and run
only **one active Master per owner**. A failover Master with the same database and
owner can recover history without the former Master's filesystem. Database
backup/replication policies cover persisted MQTT activity. Owner scoping is
independent of `[OperationalEvents] journalOwner`.

Persistence failures are displayed on the dashboard; the bounded cache continues
to update and the worker retries database writes. These retries **do not republish
MQTT messages**. Updates awaiting persistence can be lost on a crash; shutdown
allows up to five seconds for a final flush. Publishing and recording its outcome
are not an atomic transaction. Interrupted `QUEUED`/`PROCESSING` history is
recovered as `UNCONFIRMED`, not falsely reported as accepted.

Message payloads, client IDs and connection credentials are not retained; topic
and file metadata are visible only for destinations the viewer is authorized to
operate. This table is dashboard history, not the `PUBLICATION` processing queue,
the OpenTelemetry delivery queue, or a permanent audit archive.
Long metadata fields are limited to 1,024 characters; per-publication broker details
are limited to 128 Movers, with an explicit unconfirmed outcome if that limit is exceeded.

## Related

- [MQTT Overview](mqtt-overview.md)
- [Automated Data Acquisition with MQTT Client](mqtt-acquisition.md)
- [Implementation Details](implementation.md)
- [Dissemination](../use-cases/dissemination.md)
