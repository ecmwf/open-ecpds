# HTTP / HTTPS Transfer Module

!!! info
    **Two-phase flow:** The acquisition engine runs **1 LIST** to discover files at the URL in the *Directory* field, then **2 GET** to fetch each one. The sections below cover both phases.

## Listing

The **Directory** field holds the URL (or MQTT topic). Choose the mode that matches how the remote server exposes its data.

### HTML directory listing

URL returns an HTML page. OpenECPDS fetches it and extracts links using a CSS selector (Jsoup). Sub-directories are optionally followed recursively.

```properties
# Only override what differs from the defaults shown
http.dodir = "yes"             # fetch & parse the URL (default: yes)
http.select = "a[href]"        # CSS selector for link elements
http.attribute = "href"        # element attribute to extract (empty = element text)
http.listRecursive = "yes"     # follow sub-directory links
http.listMaxThreads = "10"     # parallel listing threads (default: 10)
http.listMaxWaiting = "100"    # max queued listing jobs (default: 100)
http.listMaxFiles = "500000"   # stop collecting after this many files
http.listMaxDirs = "50000"     # max sub-directories to visit
http.maxSize = "10MB"          # reject HTTP responses larger than this
http.useHead = "yes"           # HEAD per entry to get size & date (faster)
http.ftpLike = "yes"           # parse response as FTP-style lines
```

!!! info
    `http.dodir` defaults to `"yes"` — no need to set it explicitly for this mode.

### Structured data — CSV / JSON / XML / STAC

URL returns a machine-readable manifest (e.g. a NASA CMR CSV, an S3 inventory, a STAC catalog). Set `http.parser` and map columns/fields via `http.parserOptions`.

#### CSV

```properties
http.dodir = "yes"
http.parser = "csv"
# parserOptions keys (comma-separated key=value):
#   nameCol   -> 0-based column index for the file name  (default: 0)
#   urlCol    -> 0-based column index for the download URL
#   timeCol   -> 0-based column index for the timestamp  (default: disabled)
#   delimiter -> field separator                          (default: ,)
#   header    -> skip the first (header) row             (default: true)
http.parserOptions = "nameCol=0,urlCol=4,timeCol=2"

# Example - NASA CMR granules.csv:
# Granule UR | Producer ID | Start Time | End Time | Online Access URLs | ...
#  col 0        col 1         col 2        col 3       col 4
```

#### JSON

```properties
http.parser = "json"
# Defaults: arrayPath=items, nameField=name, urlField=url, timeField=time
http.parserOptions = "arrayPath=results,nameField=filename,urlField=downloadUrl,timeField=updatedAt"
```

#### STAC (SpatioTemporal Asset Catalog)

```properties
http.parser = "stac"
# Defaults: arrayPath=features, nameField=id
#           urlField=assets.data.href, timeField=properties.start_datetime
http.parserOptions = "arrayPath=features,nameField=id,urlField=assets.data.href,timeField=properties.start_datetime"
```

#### XML

```properties
http.parser = "xml"
# Defaults: arrayPath=items.item, nameField=name, urlField=url, timeField=time
http.parserOptions = "arrayPath=catalog.file,nameField=name,urlField=href"
```

!!! info
    For REST API URLs containing `?` parameters, set `http.urldir = "no"` to prevent a trailing slash from being appended automatically.

### Single file URL

The Directory URL *is* the file to download. One HEAD (or GET) is issued to obtain size and modification time; the result is a single listing entry.

```properties
http.urlIsFile = "yes"
http.urlIsFileName = "data.nc"   # optional: override the stored file name
http.useHead = "yes"             # HEAD to get size & date (recommended)
```

!!! tip
    Combine with a JavaScript Directory that builds a date-based URL dynamically.

### URL as-is — no listing fetch

Adds the URL directly to the listing without fetching its content during the LIST phase. The URL is downloaded during the subsequent GET phase.

```properties
http.dodir = "no"    # add URL to listing without fetching content
http.useHead = "no"  # no HEAD request during listing
```

!!! info
    Commonly used with a JavaScript Directory that generates multiple URL lines — each line becomes one listing entry.

### MQTT broker — real-time notifications

Subscribes to an MQTT broker. The **Directory** field is the *topic filter* (e.g. `cache/+/data/core/weather/#`). Each received message is processed by the JavaScript editor to extract file metadata.

```properties
http.mqttMode = "yes"
http.mqttScheme = "ssl"                    # ssl | tcp  (default: ssl)
http.mqttPort = "8883"                     # broker port (default: 8883)
http.mqttAwait = "PT30M"                   # how long to listen per cycle
http.mqttCleanStart = "yes"                # fresh session (no backlog on reconnect)
http.mqttKeepAliveInterval = "PT30S"       # PING interval
http.mqttConnectionTimeout = "PT30S"       # connect timeout
http.mqttMaxFiles = "2000000"              # max notifications per cycle
http.mqttSubscriberId = "my-client-id"     # MQTT client ID (auto-generated if absent)
http.mqttQos = "1"                         # QoS level: 0, 1 or 2

# Durable subscriptions (messages buffered across restarts):
http.mqttPersistence = "yes"
http.mqttPersistenceMode = "file"          # file | memory
http.mqttPersistenceDirectory = "/tmp/mqtt"
http.mqttSessionExpiryInterval = "1d"      # how long broker retains the session
```

#### Fields extracted from each MQTT message

```properties
http.mqttHref = "..."            # download URL
http.mqttSize = "..."            # file size in bytes
http.mqttTime = "..."            # timestamp (epoch ms)
http.mqttBody = "..."            # inline payload (stored as file content)
http.mqttAddPayload = "yes"      # attach raw MQTT payload bytes as the file
http.mqttAlternativeName = "..." # override the stored file name
```

#### JavaScript editor example (WIS2 / Global Broker)

```javascript
// mqttPayload (parsed JSON) and mqttTopic are injected automatically
const link = mqttPayload.links.find(l => l.rel === 'canonical');
if (link) {
  return { http: {
    mqttHref:            link.href,
    mqttSize:            link.length,
    mqttTime:            Date.parse(mqttPayload.properties.pubtime),
    mqttAlternativeName: mqttPayload.properties.data_id.split('/').pop()
  }};
}
```

### Trailing slash handling

A `/` is auto-appended to the URL unless it contains `?` or ends with `.html` / `.htm` / `.txt`. Override with:

```properties
http.urldir = "yes"   # always append /
http.urldir = "no"    # never append /  (required for REST API URLs with ?params)
```

## Auth

### 1. Static Bearer / API token

Inject a fixed token via `http.headers`. Multiple headers are separated by `\n`.

```properties
http.headers = "Authorization: Bearer <token>"
http.authheader = "no"    # disable HTTP Basic auth header (not needed with Bearer)
http.credentials = "no"   # disable credential provider

# Multiple headers - use \n as the line separator:
http.headers = "Authorization: Bearer <token>\nX-Api-Key: <key>"
```

!!! warning
    Tokens stored here persist in the database. Use **dynamic token refresh** (below) for tokens that expire.

### 2. HTTP Basic Authentication

Set **Login** and **Password** in the host *Identity* card, then enable the auth header.

```properties
http.authheader = "yes"   # send credentials in Authorization: Basic header
http.authcache = "yes"    # pre-emptively send credentials (skip 401 round-trip)
http.credentials = "yes"  # load credentials into the provider
```

### 3. Dynamic token refresh (JavaScript)

For tokens with limited lifetimes (OAuth 2, NASA Earthdata, etc.). Define a function in the **JavaScript** editor; the engine calls it automatically when the cached token is near expiry.

```properties
# In the Properties editor:
http.tokenFunction = "getAuthToken"   # JS function to call  (default: getAuthToken)
http.tokenHeader = "Authorization"    # which header to update with the new token
http.tokenLookahead = "60"            # refresh N seconds before the token expires
# The following two are managed automatically - do not set them manually:
# http.tokenValue  = ""              # current token value (written back after refresh)
# http.tokenExpiry = "-1"            # token expiry epoch-ms (written back after refresh)
```

#### In the JavaScript editor

```javascript
function getAuthToken() {
  // Available helpers:
  //   setup.get("http.login")     -> host Login field value
  //   setup.get("http.password")  -> host Password field value
  //   setup.get("http.basicAuth") -> "Basic <base64(login:password)>"
  //   http.get(url, headers)      -> { status: 200, body: "..." }
  //   http.post(url, headers, body) -> { status: 200, body: "..." }

  var resp = http.post(
    "https://auth.example.com/oauth/token",
    { "Content-Type": "application/x-www-form-urlencoded" },
    "grant_type=client_credentials&client_id=X&client_secret=Y"
  );
  var data = JSON.parse(resp.body);
  return {
    token:     "Bearer " + data.access_token,
    expiresAt: Date.now() + data.expires_in * 1000   // epoch ms
  };
}
```

!!! info
    The function must return `{ token: "...", expiresAt: <epoch ms> }`. The returned token replaces the matching header line in `http.headers` and is persisted automatically.

## Connection

### Scheme, host & port

```properties
http.scheme = "https"   # http | https  (default: http)
http.port = "443"       # port (default: 80)
```

### SSL / TLS

```properties
http.sslValidation = "no"              # skip certificate validation (dev / self-signed only)
http.protocol = "TLS"                  # SSL context protocol: TLS | TLSv1.2 | TLSv1.3
http.supportedProtocols = "TLSv1.2"   # comma-separated allowed protocols
http.strict = "no"                     # strict hostname verification
```

### Proxy

```properties
http.proxy = "http://proxy.example.com:8080"
# Leave unset (or "none") to connect directly
```

### Redirects & compression

```properties
http.maxRedirects = "5"               # max redirects to follow (default: 5)
http.allowCircularRedirects = "no"    # allow repeated redirects to the same URL
http.enableContentCompression = "yes" # accept gzip / deflate responses
```

### URL encoding & path handling

```properties
http.encodeURL = "no"       # percent-encode special characters in the URL path
http.hasParameters = "no"   # treat the URL query string as part of the file path
                            # (use "yes" when query params must survive normalisation)
```

### Upload settings (dissemination / PUT)

```properties
http.uploadEndPoint = "/upload"     # alternative PUT / POST endpoint path
http.usePost = "no"                 # use POST instead of PUT for uploads
http.useMultipart = "no"            # use multipart/form-data encoding
http.multipartMode = "LEGACY"       # LEGACY | STRICT | EXTENDED
http.filenameAttribute = "filename" # multipart field name for the filename
```

## Registration

After the LIST phase each discovered URL is evaluated by the acquisition engine. These options control which files are queued and how they are stored.

### File age filter

A JavaScript boolean expression. Variables: `$age` (ms since modification), `$time1` / `$time2` (remote / local timestamps).

```properties
acquisition.fileage = "$age < 7d"
# Accept files modified within the last 7 days

acquisition.fileage = "$age > (5*60*1000) && $age < (20*24*60*60*1000)"
# Accept files between 5 minutes and 20 days old
```

### Lifetime & metadata

```properties
acquisition.lifetime = "PT168H"
# How long to retain the file in OpenECPDS (ISO-8601 duration = 7 days)

acquisition.metadata = "targetname=$destination/data/$target[11]"
# Compute the target path/name.
# Supported tokens: $target[start..end], $date[dateformat=...],
#                   $destination, $link

acquisition.target = "$link"
# Use the listing URL as the file's target name (useful with MQTT)
```

### Requeue behaviour

```properties
acquisition.requeueonupdate = "yes"         # requeue if remote file has changed
acquisition.requeueonsamesize = "no"        # requeue even if size is identical
acquisition.requeueOnFailure = "yes"        # requeue after a retrieval failure
acquisition.requeueon = "$time2 > $time1"  # custom requeue condition expression
```

### Queue control

```properties
acquisition.standby = "no"           # queue immediately (yes = only on explicit demand)
acquisition.listSynchronous = "no"   # run listing async (yes = wait for full list)
acquisition.maximumDuration = "PT24H" # max wall-clock time for one acquisition cycle
acquisition.priority = "99"          # transfer queue priority (higher = earlier)
acquisition.uniqueByTargetOnly = "yes" # deduplicate by target name only (ignore URL)
```

### Retrieval tuning

```properties
retrieval.minimumRate = "100B"          # abort if transfer rate drops below this
retrieval.maximumDuration = "PT1H40M"   # abort if a single file takes longer
retrieval.interruptSlow = "yes"         # enable the slow-transfer kill switch
```

### Symlinks & inline payload (MQTT)

```properties
acquisition.useSymlink = "yes"         # record as symlink (typical for MQTT flows)
acquisition.payloadExtension = ".json" # append extension to inline payload files
http.mqttAddPayload = "yes"            # use MQTT message body as the file content
```

## Related

- [Transfer Modules overview](index.md)
- [MQTT Notification System](../notifications/mqtt-overview.md)
