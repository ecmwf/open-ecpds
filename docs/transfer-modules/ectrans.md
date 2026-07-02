# ECtrans Common Options

!!! info
    These options apply to **every** transfer module (FTP, SFTP, S3, Azure, etc.). They are
    configured in the host's option editor alongside module-specific options, using the
    `ectrans.` prefix. Settings here control connection retries, timeouts, stream buffering,
    compression, external handlers, notifications, and low-level TCP tuning.

## Connection & DNS

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.connectTimeOut` | Duration | `1m` | Maximum time to wait when establishing a connection to the remote site |
| `ectrans.usednsname` | Boolean | `false` | Pass the hostname verbatim to the transfer module instead of resolving it to an IP first. Useful when the target uses dynamic DNS (DDNS), virtual hosting, or certificate validation based on the hostname |
| `ectrans.hostSelector` | String | *none* | Override the hostname or IP used to connect. Accepts multi-line rules in the format `({key} {operator} {value}) {hostname-or-IP}`. Operators: `==`, `!=`, `.=` (starts-with), `=.` (ends-with). Available placeholders: `$mover`, `$host`, `$network`, `$group`. The first line may be a bare placeholder as a fallback (e.g. `$host`). Example: `($host == 192.0.2.1) backend.example.com` |
| `ectrans.debug` | Boolean | `false` | Emit additional debug messages in the data mover logs for transfer module activity on this host |

### Quick-start example

```properties
ectrans.usednsname = yes
ectrans.hostSelector = ($network == internal) 10.0.0.5
```

## Retries

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.retryCount` | Integer | `1` | Number of additional connection attempts before signalling a failure to the transfer scheduler |
| `ectrans.retryFrequency` | Duration | `1s` | Delay between retry attempts when `ectrans.retryCount` is greater than zero |

## Timeouts

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.closeTimeOut` | Duration | `1m` | Maximum time allowed for the module to close/finalise a transfer session |
| `ectrans.delTimeOut` | Duration | `90s` | Maximum time allowed for a remote file deletion |
| `ectrans.getTimeOut` | Duration | *none* | Maximum time allowed for a full file retrieval (GET). When unset, no timeout is enforced |
| `ectrans.listTimeOut` | Duration | `90s` | Maximum time allowed for a remote directory listing |
| `ectrans.mkdirTimeOut` | Duration | `90s` | Maximum time allowed for creating a remote directory |
| `ectrans.moveTimeOut` | Duration | `90s` | Maximum time allowed for a remote file move/rename |
| `ectrans.putTimeOut` | Duration | *none* | Maximum time allowed for a full file upload (PUT). When unset, no timeout is enforced |
| `ectrans.rmdirTimeOut` | Duration | `90s` | Maximum time allowed for removing a remote directory |
| `ectrans.sizeTimeOut` | Duration | `90s` | Maximum time allowed for querying a remote file size |
| `ectrans.streamTimeout` | Duration | `5m` | Maximum time allowed for the raw byte streaming phase of a PUT, excluding protocol handshake overhead (see also `ectrans.putTimeOut`) |

## Stream Buffers & Closing

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.buffInputSize` | ByteSize | *none* | Buffer size wrapping the input stream when ECtrans reads data. Unset means no extra application-level buffer |
| `ectrans.buffOutputSize` | ByteSize | *none* | Buffer size wrapping the output stream when ECtrans writes data. Unset means no extra application-level buffer |
| `ectrans.plugBuffSize` | ByteSize | `64k` | Buffer size used when ECtrans pipes the input stream into the output stream (the internal plug thread) |
| `ectrans.plugDoFlush` | Boolean | `false` | Force a flush after every write through the plug. Always flushed when the host has on-the-fly compression enabled |
| `ectrans.plugReadFully` | Boolean | `false` | Force reads to fill the entire buffer (or reach end-of-stream) before writing |
| `ectrans.closeAsynchronous` | Boolean | `true` | Close the transfer module asynchronously after data transfer completes. Speeds up dissemination under heavy load, but any failure during close will not be reported back to the scheduler |
| `ectrans.putMonitoredInputDelta` | Duration | *none* | When set, write a debug log entry showing bytes disseminated so far, repeated every time this duration elapses |

## Compression & Filters

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.filterMinimumSize` | ByteSize | `0` | Minimum file size required for compression to be applied. Files smaller than this threshold are transferred uncompressed |
| `ectrans.filterpattern` | String | `.*` | Regex pattern matched against the **original** filename to decide whether compression applies. The pattern is not applied to the final target name |
| `ectrans.checkfiltersize` | Boolean | `true` | When `true` and the compressed file is larger than the original, the original (uncompressed) file is sent instead, without the compression extension |
| `ectrans.supportFilter` | Boolean | `false` | When the remote server is a DissFTP server, enable on-the-fly decompression at the remote end during dissemination |
| `ectrans.createChecksum` | Boolean | `false` | Compute an MD5 checksum on the fly during dissemination if none has been generated yet. The checksum is then passed to the transfer module for optional use |

## External Handlers — GET

Use these to delegate file retrieval to an external process.

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.getHandler` | Boolean | `false` | Enable delegation of data retrieval to an external process. Requires `ectrans.getHandlerCmd` |
| `ectrans.getHandlerCmd` | String | *none* | Command line to execute for the external GET process. Placeholders `$source` and `$target` are substituted at runtime. Progress is parsed from output lines ending in `XX.XX%` (e.g. `progress: 12.98%`) |
| `ectrans.getHandlerAck` | String | *none* | Expected string on the last line of the external process output. A mismatch signals failure |
| `ectrans.getHandlerExitCode` | Integer | `0` | Expected exit code from the external GET process. Any other exit code signals failure |

### Quick-start example

```properties
ectrans.getHandler = yes
ectrans.getHandlerCmd = /opt/scripts/fetch.sh $source $target
ectrans.getHandlerExitCode = 0
ectrans.getHandlerAck = DONE
```

## External Handlers — PUT

Use these to delegate file dissemination to an external process, or to use the module's own copy feature.

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.putHandler` | Boolean | `false` | Enable delegation of data dissemination to an external process (via `ectrans.putHandlerCmd`) or to the module's native copy feature |
| `ectrans.putHandlerCmd` | String | *none* | Command line for the external PUT process. Placeholders `$source` and `$target` are substituted at runtime. Progress is parsed from output lines ending in `XX.XX%` (e.g. `progress: 12.98%`) |
| `ectrans.putHandlerAck` | String | *none* | Expected string on the last line of the external PUT process output. A mismatch signals failure |
| `ectrans.putHandlerExitCode` | Integer | `0` | Expected exit code from the external PUT process. Any other exit code signals failure |

### Quick-start example

```properties
ectrans.putHandler = yes
ectrans.putHandlerCmd = /opt/scripts/push.sh $source $target
ectrans.putHandlerExitCode = 0
```

## Multi-stream Retrieval

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.usemget` | Boolean | `false` | When retrieving via index files, delegate the entire multi-file retrieval directly to the transfer module (ECauth optimisation). When `false`, ECtrans iterates the index and retrieves each file individually |
| `ectrans.multipleInputStream` | String | *none* | Fine-tune parallel retrieval when `ectrans.usemget` is `false`. Comma-separated key=value pairs: `retryCount` (default `1`), `retryFrequency` (default `1000` ms), `useCache` (default `false`), `cacheSize` (default `655360` bytes), `queueSize` (default `3`). Example: `retryCount=2,queueSize=4,useCache=yes` |

## Notifications

These options fire during a PUT operation to report transfer progress or trigger downstream workflows.

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.location` | String | *empty* | Location metadata attached to notifications. Resolved in event scripts and host JavaScript via the `$location` placeholder. Supports placeholders: `$filename`, `$movername`, `$datafileid`, `$datafileuuid`, `$datatransferid`, `$datatransferuuid`, `$uuid`, `$version` |
| `ectrans.notifyPre` | String | *empty* | JavaScript snippet executed **before** the PUT begins (after connection). Connect-option placeholders are resolved before execution |
| `ectrans.notifyAuth` | String | *empty* | MQTT broker credentials used to open a notification channel before the transfer. Format: `url=mqtt://host:port;name=clientId;password=secret`. Connect-option placeholders are resolved before the connection is established |
| `ectrans.notifyPost` | String | *empty* | JavaScript snippet executed **after** the PUT completes successfully. Connect-option placeholders are resolved before execution |
| `ectrans.notifyPublish` | String | *empty* | MQTT message published after a successful PUT (requires `ectrans.notifyAuth`). Semicolon-separated key=value parameters: `topic` (or `key`), `payload` (or `url`), `metadata` (or `value`), `lifetime` (milliseconds, `-1` = no expiry). Connect-option placeholders are resolved. Example: `topic=ecpds/data/file;payload=https://example.com/data/file.bin;metadata=filename=file.bin;lifetime=3600000` |

### Notification flow

```
connect → notifyPre (JS) → notifyAuth (MQTT connect)
                                   ↓
                             [ PUT data ]
                                   ↓
                       notifyPost (JS) → notifyPublish (MQTT publish)
```

### Quick-start example

```properties
# Publish an MQTT notification after every successful PUT
ectrans.notifyAuth = url=mqtt://broker.example.com:1883;name=ecpds;password=secret
ectrans.notifyPublish = topic=ecpds/dissemination/$filename;payload=https://data.example.com/$filename;metadata=filename=$filename,version=$version;lifetime=86400000
```

## TCP Tuning

!!! warning
    These options manipulate kernel-level socket parameters. The underlying operating system
    must support each feature. Misconfiguration can reduce transfer performance or cause
    connection failures.

### General socket options

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.socketStatistics` | Boolean | `true` | Collect per-transfer socket statistics (e.g. TCP retransmits) after each dissemination PUT. Automatically disabled when a connection spool is in use |
| `ectrans.soMaxPacingRate` | ByteSize | *none* | Maximum transmit rate for the socket (bytes/s). TCP pacing smooths bursts; beneficial for flows with idle periods |
| `ectrans.tcpCongestionControl` | String | *none* | Congestion control algorithm (e.g. `bbr`, `cubic`, `reno`). Must be available on the data mover host's kernel |

### Nagle & ACK

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.tcpNoDelay` | Boolean | *OS default* | Disable the Nagle algorithm to reduce latency for small writes |
| `ectrans.tcpQuickAck` | Boolean | *OS default* | Send TCP ACKs immediately rather than waiting for the delayed-ACK timer |
| `ectrans.tcpTimeStamp` | Boolean | *OS default* | Enable or disable TCP timestamp options in packets |

### Window & segment size

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.tcpMaxSegment` | Integer | *OS default* | Maximum TCP segment size (MSS) in bytes |
| `ectrans.tcpWindowClamp` | Integer | *OS default* | Upper bound on the advertised TCP receive window |

### Keep-alive

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.tcpKeepAlive` | Boolean | *OS default* | Enable TCP keep-alive probes to detect dead connections |
| `ectrans.tcpKeepAliveTime` | Integer | *OS default* | Idle time (seconds) before the first keep-alive probe is sent |
| `ectrans.tcpKeepAliveInterval` | Integer | *OS default* | Interval (seconds) between subsequent keep-alive probes |
| `ectrans.tcpKeepAliveProbes` | Integer | *OS default* | Number of unacknowledged probes before the connection is considered dead |

### Linger

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.tcpLingerEnable` | Boolean | *OS default* | Enable the SO_LINGER socket option |
| `ectrans.tcpLingerTime` | Integer | *OS default* | Linger duration (seconds) before a closing socket is forcibly discarded |

### User timeout

| Option | Type | Default | Description |
|---|---|---|---|
| `ectrans.tcpUserTimeout` | Integer | *OS default* | Maximum time (milliseconds) that transmitted data may remain unacknowledged before an error is returned |

### Quick-start examples

```properties
# Optimise for high-latency WAN links
ectrans.tcpKeepAlive = yes
ectrans.tcpKeepAliveTime = 60
ectrans.tcpKeepAliveInterval = 10
ectrans.tcpKeepAliveProbes = 6
ectrans.tcpCongestionControl = bbr
```

```properties
# Cap throughput and disable Nagle for interactive-style feeds
ectrans.soMaxPacingRate = 100MB
ectrans.tcpNoDelay = yes
```

## Related

- [Transfer Modules overview](index.md)
- [Notifications (MQTT)](../notifications/mqtt-overview.md)
- [Hosts & Transfer Methods](../concepts/entities.md)
- [Data Transfer Lifecycle](../architecture/data-transfer-lifecycle.md)
