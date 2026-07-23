# Destination Options

!!! info
    These options configure behaviour in the **Destination editor** **Properties** field.
    They apply to a specific **Destination** and use the `scheduler.`, `incoming.`,
    `alias.` and `mqtt.` prefixes.

## `scheduler.*` — Transfer Scheduler Options

These options affect the transfer requests created for the destination queue.

### Timing & scheduling

| Option | Type | Default | Description |
|---|---|---|---|
| `scheduler.activeTimeRange` | String | *none* | Comma-separated list of time ranges during which the destination scheduler is active. Use it to process dissemination only within selected windows |
| `scheduler.asap` | Boolean | *disabled* | Set the schedule time to “now” for each submitted transfer. This overrides the `-asap` option from the `ecpds` command, but transfers can still wait behind other queued work or lower priority |
| `scheduler.delay` | Duration | *none* | Add an extra delay to the scheduled time of each submitted transfer. This is cumulative with delays already set through `ecpds -delay` or `incoming.delay` |
| `scheduler.lifetime` | Duration | *none* | Set the lifetime of submitted transfer requests. Once the lifetime expires, the transfer is marked expired and is no longer available for dissemination or download |
| `scheduler.standby` | Boolean | *none* | Change the `standby` flag of submitted transfer requests |
| `scheduler.version` | String | *none* | Set an optional version for submitted transfer requests. Supported placeholders include `$date`, `$timestamp`, `$destination`, `$target`, `$original` and `$timefile` |
| `scheduler.dateformat` | String | *platform default* | Date format used for the `$date` placeholder inside `scheduler.version` |

### Queue management & request attributes

| Option | Type | Default | Description |
|---|---|---|---|
| `scheduler.transfergroup` | String | *none* | Change the `transfergroup` assigned to submitted transfer requests |
| `scheduler.noRetrieval` | Boolean | *none* | Change the `noRetrieval` flag of submitted transfer requests |
| `scheduler.resetQueueOnChange` | Boolean | *disabled* | Reset affected destination queues when new data files arrive through the retrieval scheduler. Useful when newly queued transfers should immediately take priority over transfers already loaded in memory |
| `scheduler.masterToNotifyOnDone` | Hostname | *none* | Notify another master when a transfer completes successfully so the matching transfer on that master can be stopped. Matching is based on destination name, target name and transfer unique key; the remote master must be reachable via RMI |
| `scheduler.forceStop` | Boolean | *disabled* | Force all transfer requests submitted to this destination into `STOP` status when `failOnDestinationNotFound` is enabled on the master |

### Requeue & duplicate handling

| Option | Type | Default | Description |
|---|---|---|---|
| `scheduler.requeueon` | JavaScript expression | *none* | Check for another transfer in the same destination with the same target name but a different transfer ID, then put the current transfer on hold when the expression evaluates as a duplicate. Available variables: `$time1`, `$time2`, `$size1`, `$size2`, `$destination`, `$target` |
| `scheduler.requeuepattern` | Regex | *none* | Regex selecting target names that should be checked by `scheduler.requeueon` |
| `scheduler.requeueignore` | Regex | *none* | Regex selecting target names that should be skipped by `scheduler.requeueon` |

### Force overrides

| Option | Type | Default | Description |
|---|---|---|---|
| `scheduler.force` | Multi-line rule set | *none* | Override scheduler parameters for selected files. Supported parameters include `scheduler.lifetime`, `scheduler.delay`, `scheduler.noRetrieval`, `scheduler.asap`, `scheduler.transfergroup` and `scheduler.standby`. Rules can be single-line (`asap=yes;standby=never;pattern=E1(.*)`) or multi-line using operators `.=`, `==`, `=.`, `!=`. With `==`, a filename wrapped in `{}` is treated as a regex |

### Quick-start example — business-hours scheduling

```properties
scheduler.activeTimeRange = "06:00-18:00,20:00-22:00"
scheduler.delay = "15m"
scheduler.lifetime = "3d"
scheduler.version = "$destination-$date-$target"
scheduler.dateformat = "yyyyMMdd"
```

### Quick-start example — hold larger duplicate files

```properties
scheduler.requeueon = "$size2 > $size1"
scheduler.requeuepattern = "(.*)\.grib"
scheduler.requeueignore = "(.*)\.tmp"
```

### Quick-start example — force rules per filename

```properties
scheduler.force = "
  (== {avhrr_n.*}) standby=never;delay=2h
  (.= E1) asap=yes;transfergroup=internet
  (=. .tmp) standby=always
"
```

## `incoming.*` — Destination Portal Options

These options control how files pushed to or pulled from this **Destination** behave in the
**Data Portal**. They are destination-level settings, unlike `portal.*`, which applies to a
specific Data User.

### Upload scheduling & lifecycle

| Option | Type | Default | Description |
|---|---|---|---|
| `incoming.standby` | Boolean | *none* | Force uploaded files into standby mode |
| `incoming.priority` | Integer | *none* | Set the priority of files pushed through the Data Portal |
| `incoming.delay` | Duration | `0` | Delay the scheduled time of files pushed through the Data Portal. By default, the scheduled time is the current time |
| `incoming.lifetime` | Duration | *none* | Set the lifetime of files pushed through the Data Portal. Expired transfers become unavailable for dissemination or download |
| `incoming.version` | String | *none* | Set an optional version for uploaded files. Supported placeholders include `$date`, `$timestamp`, `$destination`, `$target`, `$original` and `$timefile` |
| `incoming.dateformat` | String | *platform default* | Date format used for the `$date` placeholder inside `incoming.version` and `incoming.metadata` |
| `incoming.event` | Boolean | *disabled* | Generate an event when a user pushes a file through the Data Portal, allowing downstream notification systems such as MQTT to react |

### Temporary upload handling

| Option | Type | Default | Description |
|---|---|---|---|
| `incoming.tmpDetect` | Boolean | *disabled* | Treat uploaded files as temporary based on `incoming.tmpPattern`. Temporary files remain in standby until the user renames them to their final name |
| `incoming.tmpPattern` | Regex | `(^\.)|(\.tmp$)` | Regex used by `incoming.tmpDetect` to identify temporary files. By default, a file is temporary when its basename starts with `.` and/or ends with `.tmp` (case-insensitive) |

### Metadata & parsing

| Option | Type | Default | Description |
|---|---|---|---|
| `incoming.metadata` | String | *none* | Define metadata for uploaded files. Supported placeholders include `$date`, `$timestamp`, `$destination`, `$target`, `$original` and `$timefile` |
| `incoming.failOnMetadataParsingError` | Boolean | *disabled* | Discard the file when `incoming.metadata` cannot be parsed successfully |

### Listing & presentation

| Option | Type | Default | Description |
|---|---|---|---|
| `incoming.rootdir` | String | Destination name | Alternate name shown for this destination path in the Data Portal |
| `incoming.sort` | String | *none* | Sort the Data Portal listing by `size`, `target` name, or `time` |
| `incoming.order` | String | *none* | Sort direction when `incoming.sort` is set: `asc` or `desc` |

### Transfer-rate limits & quotas

| Option | Type | Default | Description |
|---|---|---|---|
| `incoming.maxBytesPerSecForInput` | ByteRate | *none* | Cap the transfer rate for downloads via the Data Portal |
| `incoming.maxBytesPerSecForOutput` | ByteRate | *none* | Cap the transfer rate for uploads via the Data Portal |
| `incoming.maxUploadBytes` | ByteSize | *none* | Maximum bytes that may be uploaded to this destination within the rolling window defined by `incoming.uploadPeriod`. New upload connections are refused once the limit is reached |
| `incoming.uploadPeriod` | Duration | *none* | Rolling time window for `incoming.maxUploadBytes`. Both must be set to activate the upload quota |
| `incoming.maxDownloadBytes` | ByteSize | *none* | Maximum bytes that may be downloaded from this destination within the rolling window defined by `incoming.downloadPeriod`. New download connections are refused once the limit is reached |
| `incoming.downloadPeriod` | Duration | *none* | Rolling time window for `incoming.maxDownloadBytes`. Both must be set to activate the download quota |

### Quick-start example — portal uploads with metadata and eventing

```properties
incoming.priority = "80"
incoming.delay = "10m"
incoming.version = "$date-$target"
incoming.dateformat = "yyyyMMddHHmm"
incoming.metadata = "source=portal;target=$target;version=$date"
incoming.event = "yes"
```

### Quick-start example — temporary uploads and quotas

```properties
incoming.tmpDetect = "yes"
incoming.tmpPattern = "(.*)\.partial|(.*)\.tmp"
incoming.maxUploadBytes = "100GB"
incoming.uploadPeriod = "24h"
incoming.maxBytesPerSecForOutput = "20MB"
```

## `alias.*` — Destination Alias Options

### Conditional aliasing

| Option | Type | Default | Description |
|---|---|---|---|
| `alias.pattern` | Multi-line rule set | *none* | Select which files should be aliased to the target destination and optionally enforce alias parameters before the alias is created |

`alias.pattern` accepts comma-separated parameters such as:

- selectors: `pattern=...`, `ignore=...`
- alias attributes: `lifetime=...`, `priority=...`, `asap=...`, `event=...`, `delay=...`
- date controls for `$date`: `dateformat=...`, `datedelta=...`, `datesource=...`, `datepattern=...`
- target rewriting with placeholders such as `$name`, `$path`, `$parent`, `$destination`, `$alias` and `$date`

For multi-line rules, each line uses this format:

```text
({operator} target) parameter1=value1,parameter2=value2
```

Supported operators are `==`, `!=`, `.=` and `=.`. With `==`, a target wrapped in `{}` is
interpreted as a regex.

### Quick-start example — alias GRIB files only

```properties
alias.pattern = "pattern=(.*)\.grib,ignore=(.*)\.tmp,lifetime=P2D,priority=80,asap=yes"
```

### Quick-start example — rewrite targets per pattern

```properties
alias.pattern = "
  (== {(.*)\.dat}) lifetime=P2D,priority=80,asap=yes,event=no,delay=PT15M,target=/archive/$target
  (.= M) target=/mirror/$date/$target,dateformat=MMdd,datedelta=-1,datesource=$target[2..12],datepattern=yyyyMMddHH
"
```

## `mqtt.*` — MQTT Publishing Options

These options control MQTT notifications published when destination transfer requests
complete.

| Option | Type | Default | Description |
|---|---|---|---|
| `mqtt.publish` | Boolean | *disabled* | Publish MQTT notifications when transfer requests for this destination complete, provided those transfers triggered events through `acquisition.event` or `incoming.event` |
| `mqtt.topic` | String | `destinationName/` | MQTT topic to publish to. If the topic ends with `/` — including the default case when no topic is specified — the target name is appended |
| `mqtt.qos` | Integer | *broker/client default* | MQTT Quality of Service level: `0` (at most once), `1` (at least once), or `2` (exactly once) |
| `mqtt.expiryInterval` | Duration | Transfer expiry time | How long the broker should retain an undelivered message when subscribers are unavailable |
| `mqtt.contentType` | MIME type | *none* | Content type describing the MQTT payload format, for example `application/json` |
| `mqtt.clientId` | String | *none* | Publish messages to a specific MQTT client connected to the HiveMQ broker |
| `mqtt.retain` | Boolean | *disabled* | Set the MQTT retain flag so the broker stores the latest message on the topic and delivers it to new subscribers until it expires or is replaced |

### Quick-start example — retained JSON notifications

```properties
incoming.event = "yes"
mqtt.publish = "yes"
mqtt.topic = "ecpds/data/"
mqtt.qos = "1"
mqtt.contentType = "application/json"
mqtt.retain = "yes"
```

### Building the payload with JavaScript

When a static `mqtt.payload` property is not enough, place a JavaScript snippet in the
destination's **JavaScript** field (after the `###### END-OF-PROPERTIES ######` separator).
The script is executed once per completed transfer and must return a plain object whose
keys mirror the `mqtt.*` option names. Only the keys you return are used; everything else
falls back to the properties.

**Runtime variables** — the following `$`-prefixed placeholders are substituted into the
script source before execution:

| Variable | Description |
|---|---|
| `$filename` | Full target path as stored on the Data Mover (e.g. `destination/data/file.dat`) |
| `$filesize` | File size in bytes (integer literal) |
| `$destination` | Destination name |
| `$date` | Product date formatted as `yyyyMMdd` |
| `$time` | Product time formatted as `HHmm` |
| `$timefile` | Product date as Unix milliseconds epoch (integer literal) |
| `$uuid` | Unique transfer UUID |
| `$checksum` | File checksum (MD5 hex string, may be empty) |
| `$etag` | ETag derived from file ID and timestamp |
| `$movername` | Name of the Data Mover that processed the transfer |
| `$datatransferid` | Numeric transfer ID |
| `$datafileid` | Numeric data file ID |
| `$lifetime` | Remaining transfer lifetime in milliseconds (integer literal) |
| `$productdate` | Raw product date as Unix milliseconds epoch |
| `$metadata[key]` | Value of the metadata attribute named `key` (if present) |

**Return object structure** — the script must return an object with a `mqtt` key. Any
property listed in the table above can be overridden dynamically:

```javascript
return {
  mqtt: {
    topic:       "...",   // overrides mqtt.topic
    payload:     "...",   // required — the message body
    contentType: "...",   // overrides mqtt.contentType
    retain:      true,    // overrides mqtt.retain
    qos:         1,       // overrides mqtt.qos
    expiryInterval: "PT48H"  // overrides mqtt.expiryInterval
  }
};
```

Only `mqtt.payload` is strictly required; all other keys are optional overrides.

### Full example — WIS2-style GeoJSON notification with multi-protocol links

This example generates a [WIS2](../notifications/wmo-wis2.md)-compatible GeoJSON
notification that includes direct download links for HTTPS, SFTP, and S3.

**Properties** (in the destination's **Properties** field):

```properties
incoming.event = "yes"
mqtt.contentType = "application/json"
mqtt.expiryInterval = "PT48H"
mqtt.publish = "yes"
mqtt.retain = "yes"
```

**JavaScript** (in the destination's **JavaScript** field):

```javascript
// $-placeholders are substituted before execution
var destination = "$destination";
var filename    = "$filename";
var filesize    = $filesize;                          // integer literal
var pubtime     = new Date($timefile).toISOString();  // integer literal -> ISO string
var dataId      = destination + "/" + filename;

// Adjust host and ports for your deployment
var host      = "localhost";
var httpsPort = 7443;
var sftpPort  = 7022;

var baseUrl = "https://" + host + ":" + httpsPort + "/";
var s3Url   = "https://" + host + ":" + httpsPort + "/s3/";
var sftpUrl = "sftp://test@" + host + ":" + sftpPort + "/";

return {
  mqtt: {
    payload: JSON.stringify({
      id:       "$uuid",
      type:     "Feature",
      geometry: null,
      properties: {
        datetime:       pubtime,
        pubtime:        pubtime,
        data_id:        dataId,
        content_length: filesize
      },
      links: [
        { rel: "canonical", type: "application/octet-stream",
          length: filesize, href: baseUrl + filename,
          title: "Download via HTTPS" },
        { rel: "alternate", type: "application/octet-stream",
          length: filesize, href: sftpUrl + filename,
          title: "Download via SFTP" },
        { rel: "alternate", type: "application/octet-stream",
          length: filesize, href: s3Url + filename,
          title: "Download via S3" }
      ]
    })
  }
};
```

The resulting MQTT message published on topic `destinationName/filename` will be a
GeoJSON Feature carrying three access URLs. Any subscriber can pick the protocol that
suits them best.

!!! tip "Access control"
    To allow a data user to subscribe to these notifications, set the
    [`portal.mqttPermission`](../concepts/web-user-options.md) property on their account.
    For example, to allow subscribing to all topics: `portal.mqttPermission = "#"`.



- [Data Portal use case](../use-cases/data-portal.md)
- [Notifications (MQTT)](../notifications/mqtt-overview.md)
- [OpenECPDS entities](../concepts/entities.md)
- [Acquisition options](../use-cases/acquisition-options.md)
- [Host options](../concepts/host-options.md)
