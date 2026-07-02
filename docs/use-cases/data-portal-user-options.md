# Data User Options

!!! info
    These options configure the behaviour of the **Data Portal** for a specific Data User
    (IncomingUser). They are set in the user's **Properties** field in the monitoring
    interface (`/do/user/incoming/<login>`) using the `portal.` prefix.

## Authentication & Access

| Option | Type | Default | Description |
|---|---|---|---|
| `portal.anonymous` | Boolean | `false` | Treat this user as anonymous — no authentication required |
| `portal.accessGuide` | Boolean | `true` | Show the Access Guide button (connection instructions) in the portal UI. Set to `false` to hide it |
| `portal.loginButton` | Boolean | `true` | Show the Login button in the portal UI. Set to `false` to hide it |
| `portal.geoblocking` | String | *none* | Comma-separated list of continents, ISO country codes, and/or city names to restrict portal access by location (e.g. `Europe` or `FR,Paris`). If the source IP cannot be geolocated, access is denied |
| `portal.maxConnections` | Integer | *none* | Maximum number of concurrent connections allowed for this user |
| `portal.maxConnectionsSchedule` | String | *none* | Time-based connection limit, overriding `portal.maxConnections` for given UTC time windows. Comma-separated entries in the format `HH:MM-HH:MM=N`. Example: `00:00-08:00=200,08:00-20:00=50,20:00-24:00=100`. The `maxConnections` value is used as fallback when no range matches |

### Quick-start example

```properties
portal.maxConnections = "10"
portal.maxConnectionsSchedule = "00:00-08:00=50,08:00-18:00=10,18:00-24:00=50"
portal.geoblocking = "Europe,US"
```

## Upload & Download Quotas

| Option | Type | Default | Description |
|---|---|---|---|
| `portal.maxUploadBytes` | ByteSize | *none* | Maximum bytes the user may upload within the rolling window defined by `portal.uploadPeriod`. New connections are refused once the limit is reached. Disabled when unset |
| `portal.uploadPeriod` | Duration | *none* | Rolling window for the upload quota. Both this and `portal.maxUploadBytes` must be set to activate the quota |
| `portal.maxDownloadBytes` | ByteSize | *none* | Maximum bytes the user may download within the rolling window defined by `portal.downloadPeriod`. New connections are refused once the limit is reached. Disabled when unset |
| `portal.downloadPeriod` | Duration | *none* | Rolling window for the download quota. Both this and `portal.maxDownloadBytes` must be set to activate the quota |
| `portal.maxRangesAllowed` | Integer | *none* | Maximum number of byte ranges permitted in a single HTTP `multipart/byteranges` request |

### Quick-start example

```properties
portal.maxDownloadBytes = "100GB"
portal.downloadPeriod = "24h"
portal.maxRangesAllowed = "16"
```

## Appearance & Branding

| Option | Type | Default | Description |
|---|---|---|---|
| `portal.color` | String | *none* | Colour for the portal header and footer (e.g. `black` or `#1a2b3c`) |
| `portal.title` | String | *none* | Title displayed at the top of the portal page (e.g. `Personal Data Store`) |
| `portal.tab` | String | *none* | Browser tab title (`<title>` HTML tag) |
| `portal.footer` | String | *none* | Text displayed in the portal footer (e.g. `Copyright ECMWF`) |
| `portal.warning` | String | *none* | Warning banner displayed below the page header (e.g. `Maintenance scheduled Saturday 02:00 UTC`) |
| `portal.welcome` | String | *none* | FTP welcome banner shown to the user on connect |
| `portal.msgTop` | String | *none* | Message displayed immediately before the file listing |
| `portal.msgDown` | String | *none* | Message displayed immediately after the file listing |
| `portal.sort` | String | *none* | Sort the listing by `size`, `name`, or `time` |
| `portal.order` | String | *none* | Sort direction when `portal.sort` is set: `asc` or `desc` |
| `portal.simpleList` | Boolean | `false` | Serve the directory listing as a plain text file list instead of an HTML page. Useful for command-line access (`curl`, `wget`) |
| `portal.trafficStats` | Boolean | `true` | Show the traffic statistics panel (connection count, quota usage). Automatically hidden when no connection limit is configured |

## Navigation & Path

| Option | Type | Default | Description |
|---|---|---|---|
| `portal.destination` | String | *none* | Default destination, removing the `DATA/DESNAME` path prefix from listings. The destination must be active and associated with this user |
| `portal.domain` | String | *none* | Default domain, removing the `DATA` path prefix. Ignored when `portal.destination` is also set |

## HTTP Headers & CORS

| Option | Type | Default | Description |
|---|---|---|---|
| `portal.headerRegistry` | String | *none* | Per-extension response header rules for files served over HTTP. Multi-line; each line follows the format `({operator} {value}) {header=value;...}`. Operators: `==`, `!=`, `.=` (starts-with), `=.` (ends-with). Example: `(== {*.grib}) Content-Type=application/grib`. The headers `Accept-Ranges`, `Content-Disposition`, `ETag`, and `Last-Modified` cannot be overridden |
| `portal.corsAllowOrigin` | String | *none* | Sets `Access-Control-Allow-Origin` on HTTP GET/HEAD responses for the DNS-mapped portal path of this user. Use `*` for fully public datasets or a specific origin such as `https://app.example.com`. When set, the following headers are also added automatically: `Access-Control-Allow-Methods` (`GET, HEAD, OPTIONS`), `Access-Control-Allow-Headers` (`Range, Content-Type, Authorization`), `Access-Control-Expose-Headers` (`Content-Range, Content-Length, Accept-Ranges, ETag, Last-Modified`). OPTIONS preflight requests are answered with `204 No Content`. Leave empty (default) to fall back to the global `corsAllowOrigin` value in the `HttpPlugin` configuration |

### Quick-start example — CORS for a public open-data endpoint

```properties
portal.corsAllowOrigin = "*"
```

### Quick-start example — custom MIME types

```properties
portal.headerRegistry =
  (== {*.grib}) Content-Type=application/grib
  (== {*.nc}) Content-Type=application/x-netcdf
  (.= {*.zarr}) Content-Type=application/octet-stream
```

## Operation Permissions

Each option takes a Java regex applied to the **full path** (including domain name). The operation is permitted only when the path matches. Leave unset to allow the operation unconditionally.

| Option | Type | Default | Description |
|---|---|---|---|
| `portal.getPathPermRegex` | Regex | *none* | Restrict file download (`get`) to matching paths |
| `portal.putPathPermRegex` | Regex | *none* | Restrict file upload (`put`) to matching paths |
| `portal.deletePathPermRegex` | Regex | *none* | Restrict file deletion to matching paths |
| `portal.dirPathPermRegex` | Regex | *none* | Restrict directory listing to matching paths |
| `portal.mkdirPathPermRegex` | Regex | *none* | Restrict directory creation to matching paths |
| `portal.rmdirPathPermRegex` | Regex | *none* | Restrict directory removal to matching paths |
| `portal.renamePathPermRegex` | Regex | *none* | Restrict file rename/move to matching paths |
| `portal.sizePathPermRegex` | Regex | *none* | Restrict file size queries to matching paths |
| `portal.mtimePathPermRegex` | Regex | *none* | Restrict modification-time queries to matching paths |

### Quick-start example — allow uploads only under `/incoming`

```properties
portal.putPathPermRegex = "(.*):/data/incoming/(.*)"
```

## MQTT

| Option | Type | Default | Description |
|---|---|---|---|
| `portal.mqttPermission` | String | *none* | Comma-separated list of MQTT topic patterns this user may subscribe to. `+` matches one level; `#` (end only) matches multiple levels. Example: `ecpds/data/#,ecpds/status/+/report` |

## Auditing & History

| Option | Type | Default | Description |
|---|---|---|---|
| `portal.recordHistory` | Boolean | `true` | Record each upload/download in the data transfer history list. Disable for anonymous users that generate high request volumes |
| `portal.recordSplunk` | Boolean | `false` | Write an INH (Incoming History) log entry to Splunk for each upload/download. Only effective when Splunk is configured |
| `portal.triggerEvent` | Boolean | `true` | Allow transfer history or Splunk recording to be initiated. When `false`, all recording is suppressed regardless of `portal.recordHistory` or `portal.recordSplunk` |
| `portal.triggerLastRangeOnly` | Boolean | `false` | For HTTP `multipart/byteranges` requests, emit a single event only after the final range completes rather than one event per range |
| `portal.updateLastLoginInformation` | Boolean | `true` | Record the last-login timestamp for this user. Consider disabling for anonymous/high-frequency users to avoid database flooding |

## Related

- [Data Portal use case](../use-cases/data-portal.md)
- [ECtrans Common Options](../transfer-modules/ectrans.md) — options that apply to every transfer module
- [Notifications (MQTT)](../notifications/mqtt-overview.md)
- [INH event fields](../event-logging/inh-fields.md)
- [DEA event fields](../event-logging/dea-fields.md)
