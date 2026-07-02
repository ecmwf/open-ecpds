# Acquisition Options

!!! info
    These options configure the **acquisition** process for a specific host. They are set
    in the host's **Properties** field in the monitoring interface under the `acquisition.`
    prefix, and in the host's **Directory** editor where the remote paths are defined.
    The `retrieval.*` options (also on this page) control how individual file downloads
    are rate-limited and timed out.

## File Discovery & Filtering

| Option | Type | Default | Description |
|---|---|---|---|
| `acquisition.action` | String | `queue` | Action taken on discovered files: `queue` to register them for retrieval/dissemination, or `delete` to delete them on the remote site |
| `acquisition.wildcardFilter` | String | *none* | Wildcard filter applied directly by the transfer module during listing — limits files returned at source. Preferred over `acquisition.regexPattern` when the transfer module supports it |
| `acquisition.regexPattern` | String | *none* | Regex filter applied at the data-mover level on listing output. Use when the transfer module does not support wildcard filtering. Placeholders `$date` and `$namePattern` are substituted before evaluation |
| `acquisition.fileage` | String | *none* | Age-based file filter. Simple forms: `>10d`, `<=5d`. When `$age` is detected the expression is evaluated as JavaScript with `$age` replaced by the file timestamp in milliseconds (e.g. `$age > (5*60*1000) && $age < (20*24*60*60*1000)`) |
| `acquisition.filesize` | String | *none* | Size-based file filter. Simple forms: `>10kb`, `<=5mb`. When `$size` is detected the expression is evaluated as JavaScript with `$size` replaced by the file size in bytes |
| `acquisition.onlyValidTime` | Boolean | `false` | When `true`, discard files whose timestamp cannot be read or parsed. When `false`, all files are selected regardless of timestamp validity (those files are exempt from `acquisition.fileage` checks) |
| `acquisition.useSymlink` | Boolean | *none* | Whether symbolic links found in the listing should be included (`true`) or excluded (`false`) |
| `acquisition.removeParameters` | Boolean | `false` | Strip HTTP query parameters (everything from `?` onwards) from filenames when listing output contains URLs |
| `acquisition.debug` | Boolean | `false` | Emit extra debug messages in the master logs for this host's acquisition process |

## Date Parsing

These options control how `$date` and `$dirdate` placeholders are resolved in `acquisition.metadata` and `acquisition.target`.

| Option | Type | Default | Description |
|---|---|---|---|
| `acquisition.datesource` | String | *none* | Source string to parse the date from (e.g. a filename segment). When unset, the current date/time is used |
| `acquisition.dateformat` | String | `yyyyMMdd` | Java `SimpleDateFormat` pattern used to format the resolved date |
| `acquisition.datedelta` | Duration | *none* | Offset applied to the resolved date. Positive = forward in time, negative = backward |
| `acquisition.datepattern` | String | *none* | Java `SimpleDateFormat` pattern used to **parse** the string from `acquisition.datesource` |
| `acquisition.defaultDateFormat` | String | *none* | Default date format expected by the FTP server in listing output |
| `acquisition.recentDateFormat` | String | *none* | Recent-date format expected by the FTP server (used when dates are displayed in a different format for recent files) |
| `acquisition.serverTimeZoneId` | String | *none* | Time zone of the FTP server (e.g. `UTC`, `America/New_York`) |
| `acquisition.serverLanguageCode` | String | *none* | Language code of the FTP server (affects month name parsing in listings) |
| `acquisition.shortMonthNames` | String | *none* | Space-separated abbreviated month names if the server uses non-standard abbreviations |
| `acquisition.systemKey` | String | *none* | FTP server system key for selecting the correct listing parser |
| `acquisition.regexFormat` | String | *none* | Regex describing how the FTP server listing line is split into file attributes (see Apache Commons Net FTP file entry parser) |

## File Registration

When a new file is discovered it is registered as a data transfer. These options control how it is registered.

| Option | Type | Default | Description |
|---|---|---|---|
| `acquisition.target` | String | *none* | Target filename for the registered transfer. Overrides the source filename. Placeholders: `$destination`, `$name`, `$target`, `$original`, `$link`, `$dirdate`, `$date`, `$timestamp` (epoch ms) |
| `acquisition.metadata` | String | *none* | Metadata attached to the registered transfer. Comma-separated `key=value` pairs. Same placeholders as `acquisition.target` |
| `acquisition.lifetime` | Duration | *none* | Lifetime of the registered transfer. Expired transfers are marked unavailable for dissemination or download |
| `acquisition.priority` | Integer | *none* | Dissemination queue priority for the registered transfer (higher = earlier) |
| `acquisition.standby` | Boolean | `false` | Register the transfer in standby mode (not processed until explicitly released) |
| `acquisition.noretrieval` | Boolean | `false` | When `true`, the file is not downloaded to the data movers. Instead it is accessed on-demand via the source host |
| `acquisition.deleteoriginal` | Boolean | `false` | Delete the file from the remote site after it has been successfully retrieved |
| `acquisition.version` | String | *none* | *(inherited from scheduler context)* Optional version string attached to the transfer |
| `acquisition.groupby` | String | `ACQ_{destination}_{host}` | Group-by label assigned to the registered transfer |
| `acquisition.transferGroup` | String | *none* | Transfer group for processing the registered request |
| `acquisition.event` | Boolean | `false` | Emit an event (e.g. MQTT notification) once the file has been retrieved and disseminated or made available in the Data Portal |

## Deduplication

| Option | Type | Default | Description |
|---|---|---|---|
| `acquisition.uniqueByTargetOnly` | Boolean | `false` | Use only the target name (not the source URL) as the deduplication key |
| `acquisition.uniqueByNameAndTime` | Boolean | `false` | Include the file timestamp in the deduplication key when a valid timestamp is available |
| `acquisition.useTargetAsUniqueName` | Boolean | `false` | When `acquisition.uniqueByTargetOnly` is enabled, use the target name rather than the original name for the key |

## Requeue Behaviour

| Option | Type | Default | Description |
|---|---|---|---|
| `acquisition.requeueonupdate` | Boolean | `false` | Requeue the transfer when the file is rediscovered with a newer timestamp AND a different size. Equivalent to setting `acquisition.requeueon = "$time2 > $time1 && $size2 != $size1"` |
| `acquisition.requeueonsamesize` | Boolean | `false` | Requeue even when only the timestamp has changed (size is the same). Equivalent to `acquisition.requeueon = "$time2 > $time1"`. Only used when `acquisition.requeueonupdate` is set and `acquisition.requeueon` is not |
| `acquisition.requeueOnFailure` | Boolean | `false` | Requeue the transfer after a retrieval failure. Useful for MQTT-based acquisition where notifications are sent only once |
| `acquisition.requeueon` | String | *none* | Custom JavaScript boolean expression controlling requeue behaviour. Variables: `$size1` (original size), `$size2` (new size), `$time1` (original timestamp), `$time2` (new timestamp), `$destination`, `$target`, `$original`. Takes precedence over `requeueonupdate` / `requeueonsamesize` |
| `acquisition.skipPostRetrievalSizeCheckPattern` | String | *none* | Regex pattern matching filenames for which the post-retrieval size check (comparing discovered size vs. retrieved size) should be skipped |

## Queue & Listing Control

| Option | Type | Default | Description |
|---|---|---|---|
| `acquisition.listParallel` | Boolean | `false` | Process multiple directories from the directory editor simultaneously |
| `acquisition.listMaxThreads` | Integer | *system* | Maximum concurrent connections for parallel directory listing (requires `acquisition.listParallel`) |
| `acquisition.listMaxWaiting` | Integer | *system* | Maximum queued listing jobs when parallel listing is enabled. Adding beyond this limit blocks until a slot is free |
| `acquisition.listSynchronous` | Boolean | `false` | When `true`, wait for the full listing to complete before starting to process entries. Disable for MQTT sources where the listing never ends |
| `acquisition.maximumDuration` | Duration | *none* | Maximum wall-clock time for one acquisition listing cycle |
| `acquisition.interruptSlow` | Boolean | *system* | Kill the listing when `acquisition.maximumDuration` is exceeded. Falls back to the system-wide value when unset |

## MQTT Payload

| Option | Type | Default | Description |
|---|---|---|---|
| `acquisition.payloadExtension` | String | *none* | Extension appended to the inline-payload file created alongside the data file when an MQTT notification is received |

### Quick-start examples

```properties
# Basic acquisition: requeue if file changes, expire after 7 days
acquisition.lifetime = "P7D"
acquisition.requeueonupdate = "yes"
acquisition.deleteoriginal = "no"
```

```properties
# Date-based target rename: rename to date extracted from filename
acquisition.datesource = "$target[2..12]"
acquisition.datepattern = "yyyyMMddHH"
acquisition.datedelta = "-1d"
acquisition.dateformat = "MMdd"
acquisition.target = "/archive/$date/$name"
```

```properties
# Parallel listing of many directories
acquisition.listParallel = "yes"
acquisition.listMaxThreads = "5"
acquisition.listSynchronous = "no"
acquisition.maximumDuration = "PT2H"
acquisition.interruptSlow = "yes"
```

```properties
# MQTT acquisition: always requeue on failure, process payload inline
acquisition.requeueOnFailure = "yes"
acquisition.payloadExtension = ".json"
acquisition.event = "yes"
```

---

## Retrieval Rate Control

These options limit how fast individual files are downloaded from the remote site during acquisition.

| Option | Type | Default | Description |
|---|---|---|---|
| `retrieval.minimumRate` | ByteSize/s | *none* | Abort the retrieval if the transfer rate drops below this threshold after `retrieval.minimumDuration` has elapsed |
| `retrieval.minimumDuration` | Duration | *none* | Grace period before rate and duration checks begin |
| `retrieval.maximumDuration` | Duration | *none* | Abort the retrieval if it exceeds this wall-clock time |
| `retrieval.rateThrottling` | ByteSize/s | *none* | Cap the retrieval rate to this maximum |
| `retrieval.interruptSlow` | Boolean | `false` | Enable the slow-transfer kill switch (requires `retrieval.maximumDuration` or `retrieval.minimumRate`) |

### Quick-start example

```properties
retrieval.minimumRate = "100kB"
retrieval.minimumDuration = "PT1M"
retrieval.maximumDuration = "PT2H"
retrieval.interruptSlow = "yes"
retrieval.rateThrottling = "50MB"
```

## Related

- [Use Cases — Acquisition](acquisition.md)
- [HTTP / HTTPS Transfer Module](../transfer-modules/http.md) — `acquisition.*` options appear in the HTTP module guide too
- [Destination Options](../concepts/destination-options.md) — scheduler, incoming, alias, mqtt options set on the destination
- [Host Options](../concepts/host-options.md) — ecpds, proxy, upload options
- [Notifications (MQTT)](../notifications/mqtt-overview.md)
- [ECtrans Common Options](../transfer-modules/ectrans.md)
