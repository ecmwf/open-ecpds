# Acquisition Directory

!!! info
    All options use the `acquisition.` prefix in the host *Properties* field. Most options can also be set per directory line in the `[options]` prefix block, in which case they override the global host setting for that line only.

!!! info
    For **Acquisition** hosts the Directory field is a listing specification: each line describes one remote directory to scan for files to retrieve. The entire field may also be a **JavaScript** or **Python** script that returns the listing lines dynamically at runtime. The script runs on a DataMover via `TransferScheduler.execution()`.

## How it works

### Scheduler cycle

The **AcquisitionScheduler** runs continuously inside the MasterServer. On each cycle it queries every destination that has acquisition enabled, then iterates over the Acquisition hosts associated with those destinations. For each (destination, host) pair that is not already running, it spawns an **AcquisitionThread** to process that host.

A host is only picked up when its *Acquisition Time* has elapsed since the last run. The thread count is capped by `Scheduler.maxAcquisitionThreads` (default 100). If the cap is reached the scheduler waits until threads finish before starting new ones.

### AcquisitionThread lifecycle

1. **Read the Directory field.** Each non-empty line is a *listing spec*. If the entire content is wrapped in `$(...)` it is evaluated as a script on a DataMover first; the output of the script is then treated as the listing spec lines.
2. **Parse each line** — extract inline `[options]`, trailing `{regex}` filename filter, `$date` tokens, and optional wildcard filter — and build the final remote path.
3. **List the remote directory** via a DataMover using the configured transfer module (FTP, SFTP, S3, HTTP, …). The raw listing output is stored in the *Directory Listings* panel.
4. **Parse each file entry** using an FTP-style parser (`systemKey`, date formats, language code). Each entry is checked against the file selection filters.
5. **Act on matched entries**: schedule them for retrieval (`action=queue`) or delete them from the remote server (`action=delete`).
6. **Write progress** to the *Progress* panel (the console log above) after each listing step.

### Progress panel vs Directory Listings

| Panel | Shows |
|---|---|
| **Progress** (top) | Console log: connection attempts, timing, per-file outcome (*selected / not-selected / error*), summary counts |
| **Directory Listings** (below) | Raw file listing from the remote server, parsed into structured blocks per path |

## Directory configuration

### Plain text mode

Each non-empty line describes one remote directory to scan. All parts except the path are optional. Blank lines and lines starting with `#` are ignored.

### Line syntax

```
[option1=value1;option2=value2]/path/to/$date/{filename-regex}
```

Each non-empty line in the *Directory* field describes one remote path to list. All parts except the path are optional.

| Part | Example | Description |
|---|---|---|
| `[options]` | `[filesize=>>1000;fileage=<<24h]` | Per-line option overrides, separated by `;` or `,`. Override host-level `acquisition.*` settings for this line only. |
| path | `/data/feed/$date/` | Remote directory path. Supports `$date`, `$dirdate`, and `$host[field]` tokens. |
| `{regex}` | `{.*\.nc}` | Java regex matched against each filename. Only entries whose name matches are selected. Omit to match all files. |

The host form guide also shows this equivalent line structure:

| Part | Example | Description |
|---|---|---|
| `[options]` | `[filesize=>1000;fileage=<24h]` | Per-line option overrides (separated by `;` or `,`). Override host-level `acquisition.*` properties for this line only. |
| path | `/data/feed/$date/` | Remote directory path. Supports `$date`, `$dirdate`, and `$host[field]` tokens (substituted before execution). |
| `{regex}` | `{.*\.nc}` | Java regex matched against each filename. Only matching entries are selected. Omit to match all files. |

### Plain text examples

```
/data/ecmwf/$date/{.*\.grib2}
[fileage=<6h]/data/ecmwf/$date[datedelta=-1d]/{.*\.grib2}
# older data with size filter
[filesize=>1024;fileage=<48h]/archive/$date[dateformat=yyyy/MM/dd]/
```

### Date tokens

| Token | Description |
|---|---|
| `$date` | Replaced with the current date/time formatted according to `acquisition.dateformat` (default `yyyyMMdd`) and shifted by `acquisition.datedelta`. |
| `$date[dateformat=…,datedelta=…]` | Per-token overrides. E.g. `$date[dateformat=yyyy/MM,datedelta=-1d]` gives yesterday's year/month. |
| `$date[datesource=…,datepattern=…]` | Parse date from an external string instead of using the current time. `datesource` is the string, `datepattern` is the Java `SimpleDateFormat` pattern to parse it. |
| `$dirdate` | Same as `$date` but uses options from the line options block (`[...]`) rather than the global host settings. |
| `$timestamp` | Replaced with the file's modification time in milliseconds (Unix epoch, rounded to seconds). Available in `acquisition.target`, `acquisition.metadata` etc. |

### Host field tokens

Available in the Directory field for dynamic path construction:

`$host[name]` `$host[nickname]` `$host[host]` `$host[login]` `$host[comment]` `$host[userMail]` `$host[networkCode]` `$host[networkName]` `$transferMethod[name]` `$ectransModule[name]`

The host form guide also lists these tokens as substituted before plain-text lines are processed or before the script runs.

#### Host

`$host[name]` `$host[comment]` `$host[host]` `$host[login]` `$host[passwd]` `$host[userMail]` `$host[networkCode]` `$host[networkName]` `$host[nickname]`

#### Transfer Method & Module

`$transferMethod[name]` `$transferMethod[comment]` `$transferMethod[value]` `$ectransModule[name]`

#### Date

`$date` `$date[dateformat=…,datedelta=…]` `$dirdate`

### Script mode

If the entire Directory field content is wrapped in `$( ... )`, it is executed as a script on an allocated DataMover. The standard output of the script is then interpreted as the listing spec lines (one path per line). This allows dynamic directory list generation.

!!! note
    Select **JavaScript** or **Python** mode using the radio buttons above the editor — the wrapper is added automatically on save. The script runs on an allocated DataMover and its **standard output** is interpreted as the listing specification — one path (with optional options and regex) per line, exactly as in Plain Text mode. Variables (`$host[…]`, `$transferMethod[…]`, etc.) are substituted *before* the script runs.

#### JavaScript example

```javascript
// Build directory lines dynamically — one per output line
var today = new Date();
var yyyy  = today.getFullYear();
var mm    = String(today.getMonth() + 1).padStart(2, '0');
var dd    = String(today.getDate()).padStart(2, '0');
var base  = "/data/feed/" + yyyy + "/" + mm + "/" + dd + "/";
base + "{.*\\.nc}\n" +
"[fileage=<48h]" + base.replace(dd, String(today.getDate()-1).padStart(2,'0')) + "{.*\\.nc}";
```

#### Python example

```python
from datetime import datetime, timedelta

base = "/data/feed/"
for delta in range(3):  # today and 2 previous days
    day = datetime.utcnow() - timedelta(days=delta)
    age = delta * 24
    print("[fileage=<" + str(age + 24) + "h]" + base + day.strftime("%Y/%m/%d") + "/{.*\\.nc}")
```

Use the **Test on Server** button to execute the script and inspect its output before saving.

### Test on Server

The **Test on Server** button in the Directory card header sends the current editor content to the host server and runs it in the same environment used during real transfers — including the correct DataMover, transfer method credentials, and variable substitution. The result is displayed in a panel below the editor.

**How it works:**

- For **Plain Text** mode: the listing spec lines are evaluated as-is against the remote host. Each line is resolved and the resulting remote paths are returned.
- For **JavaScript** and **Python** modes: the script is executed on an allocated DataMover; its standard output is returned and displayed exactly as the scheduler would see it at runtime.

**Tips:**

- All `$host[…]`, `$transferMethod[…]`, and `$date` variables are substituted before execution, so the output reflects real runtime values.
- The button is **disabled** while the editor contains validation errors (unrecognised options or type mismatches) — fix those first.
- A running test can be cancelled; the result panel shows success, error, or timeout status.
- Use this to verify path patterns, debug regex filters, and check that date-based expressions produce the expected directory lines before committing a change.

### Wildcard filter

The `acquisition.wildcardFilter` option appends a glob-style wildcard to the path sent to the remote server before listing. This lets the transfer module filter server-side, reducing the number of entries returned. The `acquisition.regexPattern` option applies an additional server-side regex filter at the listing call level.

## File selection

### Selection pipeline

Each file entry returned by the remote listing passes through the following checks in order. A file is *selected* only if all checks pass.

| Check | Option(s) | Default | Description |
|---|---|---|---|
| Entry type | `acquisition.useSymlink` | `false` | Directories and entries of unknown type are always skipped. Symbolic links are skipped unless `useSymlink=true`. |
| Date parseable | `acquisition.onlyValidTime` | `false` | If `true`, entries whose modification date could not be parsed are rejected. |
| Name pattern | *(from `{regex}` in line)* | match all | The filename is matched against the Java regex in the `{...}` suffix of the directory line. Non-matching entries are skipped. |
| File size | `acquisition.filesize` | no limit | Comparison expression: operator + value. Operators: `==` `!=` `>>` `<<` `>=` `<=`. Value can use units (e.g. `1MB`). Can also be a JS expression referencing `$size` (bytes). Symlinks bypass this check. |
| File age | `acquisition.fileage` | no limit | Same operator syntax but compared against the file's age (current time minus modification time). Value is a duration (e.g. `24h`, `7d`). Can also be a JS expression with `$age` (milliseconds). Skipped if file time is unknown (`-1`). |
| URL params | `acquisition.removeParameters` | `false` | For HTTP/URL entries only: if `true`, the query string (`?...`) is stripped from the filename when computing the target name. |

### Listing parser options

When the transfer module returns a raw FTP-style listing, the following options control how it is parsed:

| Option | Default | Description |
|---|---|---|
| `acquisition.systemKey` | `UNIX` | Server OS/format. Values: `UNIX`, `VMS`, `WINDOWS`, `OS/2`, `OS/400`, `AS/400`, `MVS`, `NETWARE`, `MACOS PETER`. |
| `acquisition.regexFormat` | — | Custom regex to parse non-standard listing lines. Overrides the `systemKey` format. |
| `acquisition.defaultDateFormat` | — | Java `SimpleDateFormat` pattern for entries that include full date+time (e.g. `yyyy-MM-dd HH:mm`). |
| `acquisition.recentDateFormat` | — | Pattern for entries that only include month/day/time (no year), used when the file is recent. |
| `acquisition.serverLanguageCode` | `en` | ISO 639-1 language code for month name parsing (e.g. `fr`, `de`). |
| `acquisition.shortMonthNames` | — | Comma-separated custom short month names, overriding the language-specific defaults. |
| `acquisition.serverTimeZoneId` | — | Java timezone ID for interpreting listing timestamps (e.g. `UTC`, `America/New_York`). |

## Scheduling

### Action

| Option | Default | Description |
|---|---|---|
| `acquisition.action` | `queue` | `queue` — register the file in the database for retrieval by the AcqDownloadScheduler. `delete` — delete the matched file from the remote server (no scheduling). |

### Uniqueness & deduplication

Before scheduling, the scheduler checks whether the file already exists in the database using a *unique key* built from the destination name, effective target filename, and optionally the modification time.

| Option | Default | Description |
|---|---|---|
| `acquisition.uniqueByTargetOnly` | `false` | If `true`, the unique key is the target filename only (ignoring the original full path). Two files with the same target name are treated as the same file regardless of their remote location. |
| `acquisition.useTargetAsUniqueName` | `false` | Use the *initial* target (before `acquisition.target` substitution) as the uniqueness key rather than the original full path. |
| `acquisition.uniqueByNameAndTime` | `false` | Append the file modification timestamp to the unique key so that the same filename with a different mtime is treated as a new file. |

### Re-queueing

If a file already exists in the database, the scheduler evaluates whether it should be re-scheduled.

| Option | Default | Description |
|---|---|---|
| `acquisition.requeueon` | — | A JavaScript expression that evaluates to `true` to re-queue. Variables: `$time1`/`$size1` (existing DB record), `$time2`/`$size2` (new remote values, in ms and bytes), `$destination`, `$target`, `$original`. Example: `$time2 > $time1 && $size2 != $size1` |
| `acquisition.requeueonupdate` | `false` | Legacy shortcut: re-queue when the remote modification time is newer than the stored one. Ignored if `requeueon` is set. |
| `acquisition.requeueonsamesize` | `false` | Legacy shortcut: when combined with `requeueonupdate`, also re-queue when size differs. Ignored if `requeueon` is set. |
| `acquisition.requeueOnFailure` | `false` | If `true` and a retrieval fails, put the transfer back to `SCHE` (re-scheduled) rather than marking it `FAIL`. Allows automatic retry on next acquisition cycle. |

### Target & metadata

| Option | Default | Description |
|---|---|---|
| `acquisition.target` | `$target` | Template for the stored filename. Tokens: `$target` (initial filename), `$name` (basename without path), `$original` (full remote path), `$link` (symlink target if any), `$destination`, `$date`, `$dirdate`, `$timestamp` (file mtime in ms, rounded to seconds). |
| `acquisition.metadata` | *(empty)* | Arbitrary metadata string stored with the scheduled transfer. Supports the same tokens as `target`. Can be used downstream by dissemination rules. |

### Retrieval parameters

| Option | Default | Description |
|---|---|---|
| `acquisition.priority` | `99` | Scheduling priority for the queued transfer (lower number = higher priority). |
| `acquisition.lifetime` | `2d` | How long the scheduled transfer is kept alive in the database before expiry. |
| `acquisition.standby` | `false` | If `true`, the transfer is created in standby mode and will not be retrieved until explicitly released. |
| `acquisition.groupby` | *auto* | Group key used to batch transfers together (default: `ACQ_{destination}_{host}`). |
| `acquisition.transferGroup` | — | Override the transfer group used for the retrieval DataMover selection. |
| `acquisition.event` | `false` | If `true`, trigger an event notification when the file is scheduled. |
| `acquisition.noretrieval` | `false` | Register the transfer in the database but skip the actual retrieval (metadata-only scheduling). |
| `acquisition.deleteoriginal` | `false` | Delete the remote file after it has been successfully retrieved. |
| `acquisition.payloadExtension` | `.payload` | File extension appended to notification payload filenames when a message body is included in the listing entry. |

## Advanced

### Performance & parallelism

| Option | Default | Description |
|---|---|---|
| `acquisition.listSynchronous` | `true` | If `true`, the remote listing call blocks until the full directory listing is received before parsing begins. If `false`, listing and parsing run in a pipeline (listing data is streamed as it arrives). |
| `acquisition.listParallel` | `false` | If `true`, individual file entries within a single listing are processed in parallel using a thread pool (speeds up database checks and inserts for large directories). |
| `acquisition.listMaxWaiting` | `100` | Maximum number of file entries queued for parallel processing before the listing reader pauses. |
| `acquisition.listMaxThreads` | `100` | Maximum number of parallel threads used when `listParallel=true`. |

### Timeout & interruption

| Option | Default | Description |
|---|---|---|
| `acquisition.maximumDuration` | *server default (10 min)* | Per-host maximum run time. If the AcquisitionThread runs longer than this it is automatically interrupted. Overrides the server-wide `Scheduler.maximumDurationAcquisitionThread` setting for this host. |
| `acquisition.interruptSlow` | *server default* | Override the server-wide `Scheduler.interruptSlowAcquisitionThread` flag for this host. Set to `false` to allow this host to exceed the maximum duration without being killed. |

### Pattern matching

| Option | Default | Description |
|---|---|---|
| `acquisition.wildcardFilter` | *(empty)* | Glob-style string appended to the path before sending the list request to the remote server. Reduces the number of entries the DataMover returns. E.g. `*.nc` sends `/data/feed/20240101/*.nc` to the server. |
| `acquisition.regexPattern` | *(empty)* | Regex passed to the DataMover to filter entries at the listing call level (before the AcquisitionThread receives them). Complements the per-line `{regex}` filter. |
| `acquisition.skipPostRetrievalSizeCheckPattern` | — | Regex matched against the source filename during retrieval. If it matches, the post-retrieval size verification step is skipped. Useful for servers that report incorrect file sizes. |

### Diagnostics

| Option | Default | Description |
|---|---|---|
| `acquisition.debug` | `false` | If `true`, each parsed file entry and option is also written to the MasterServer debug log in addition to the Progress panel. |

## Related

- [Host Directory Field](index.md)
- [Dissemination Directory](dissemination.md)
- [Replication, Source, Backup & Proxy Directory](replication.md)
- [Transfer Modules](../transfer-modules/index.md)
