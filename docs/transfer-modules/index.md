# Transfer Modules

OpenECPDS is built around a **modular transfer system** (internally referred to as
*ECtrans modules*). Each module implements a specific network protocol or storage
backend, and is selected for a transfer based on the [Transfer Method](../concepts/entities.md#transfer-methods)
configured on a [Host](../concepts/entities.md#dissemination-and-acquisition-hosts).
Because the system is modular, new protocols can be added through extensions without
changing the core of OpenECPDS.

A module is responsible for the low-level operations of a transfer:

- **connect** — establish a connection (or authenticate) to the remote system.
- **put / get** — upload or download file content.
- **size** — verify the size of a transferred object.
- **del** — delete a remote object (where supported).
- **copy** — server-side or simulated copy (where supported).

## Supported protocols & backends

| Module | Protocol / backend | Direction | Page |
|--------|--------------------|-----------|------|
| FTP | File Transfer Protocol | Dissemination & Acquisition (also incoming Data Portal) | [FTP](ftp.md) |
| FTPS | FTP over TLS/SSL | Dissemination & Acquisition | [FTPS](ftps.md) |
| SFTP | SSH File Transfer Protocol | Dissemination & Acquisition | [SFTP](sftp.md) |
| HTTP / HTTPS | HyperText Transfer Protocol (with embedded MQTT client) | Dissemination & Acquisition | [HTTP/HTTPS](http.md) |
| WebDAV | Web Distributed Authoring and Versioning (RFC 4918) | Dissemination & Acquisition (also incoming Data Portal) | [WebDAV](webdav.md) |
| S3 | Amazon S3 object storage | Dissemination & Acquisition | [Amazon S3](s3.md) |
| GCS | Google Cloud Storage | Dissemination & Acquisition | [Google Cloud Storage](gcs.md) |
| Azure | Azure Blob Storage | Dissemination & Acquisition | [Azure Blob Storage](azure.md) |
| ECauth | SSH/Telnet authentication transport | Dissemination & Acquisition | [ECauth](ecauth.md) |
| ECaccess (optional extension) | External ECaccess gateway through ECCMD | Dissemination & Acquisition | [ECaccess](ecaccess.md) |
| Portal | No-op local staging for Data Portal pull | Dissemination | [Portal](portal.md) |
| Test | Simulation / benchmarking | Dissemination | [Test](test.md) |

For a higher-level discussion of incoming and outgoing protocols, see
[Protocols & Connections](../concepts/protocols.md).

## Option syntax

Each module exposes a set of configuration options. Options are written as
`key=value` pairs, one per line, in the host's option editor. Every option is
namespaced with the module's prefix — for example `ftp.port`, `sftp.cipher`,
`s3.bucketName`, `http.*`, `test.bytesPerSec`. Options that are not relevant to the
selected module are ignored.

In addition to module-specific options, a set of [ECtrans common options](ectrans.md)
(`ectrans.*`) applies to every module and controls shared behaviour such as connection
retries, timeouts, stream buffering, compression, external handlers, notifications,
and TCP-level tuning.

```properties
# Example: a few SFTP options
sftp.port = "22"
sftp.cipher = "aes128-ctr"
```

Values commonly accept human-friendly notation:

- **Byte sizes** — e.g. `10MB`, `1GB`.
- **Durations** — e.g. `30s`, `5m`, `PT1H30M`. Accepts shorthand suffixes (`s`=seconds, `m`=minutes, `h`=hours, `d`=days, `w`=weeks, `y`=years) or full ISO-8601 notation (e.g. `PT30S`, `PT0.5S`).
- **Booleans** — `yes`/`no` or `true`/`false` (see each module page for specifics).

## Where modules fit

- The [Mover Server (Data Mover)](../architecture/components.md#mover-server-data-mover)
  loads the appropriate module at transfer time.
- The [Directory field](../host-directory/index.md) of a host controls *where* the
  file is read from or written to on the remote system, independently of the module.
- The [data-transfer lifecycle](../architecture/data-transfer-lifecycle.md) describes
  how transfers move through their statuses, including failures and retries handled at
  the module level.

## Password rotation capability

Password rotation is protocol-neutral and opt-in. A module advertises support
by overriding `TransferModule.passwordRotation()` and returning a
`PasswordRotation` implementation. Its contract includes:

- Parsing and fingerprinting an operator-approved endpoint, including all
  protocol-specific identity checks, without credentials.
- Checking the exact transfer-method template, Host options and port in
  `supports`, without network or other external I/O. Reject options that bypass
  the supplied password, override the approved endpoint or select another
  authentication mode.
- Probing replacement credentials in isolation, including write/cleanup checks.
- Connecting the actual module instance using the supplied password directly,
  never a connection URL, a password from the Host configuration, a log message
  or a reusable cookie.
- Implementing `TransferModule.isSessionClosureConfirmed()` accurately. A normal
  return from `close()` is not sufficient evidence when disconnect errors are
  suppressed. Unknown outcomes must remain unconfirmed.

No protocol switch or core registration is required. Modules without this
capability remain ineligible. The built-in adapters are:

| Module | Rotated credential | Encrypted connection |
|---|---|---|
| `JSftpModule`, `SftpModule` | Password | Pinned SSH host key |
| `FtpsModule` | Password | Explicit FTPES or implicit FTPS, verified certificate and private data channel |
| `HttpModule` | Basic password or bearer token | Verified HTTPS file endpoint: PUT, HEAD and DELETE |
| `WebDavModule` | Password | Verified HTTPS WebDAV |
| `AmazonS3Module` | Access-key pair, optionally a session token | Verified HTTPS, fixed origin/region/bucket, path-style addressing |
| `AzureModule` | Shared account key, OAuth access token or direct SAS token | Verified HTTPS, fixed origin/container |
| `GcsModule` | Complete service-account key or OAuth access token | Verified HTTPS, fixed API origin/project/bucket and approved token endpoint |

These are opt-in connection paths that accept only supported module options.
SSH pinning, password-only authentication, port 22 and empty
Host-options restrictions remain specific to `JSftpModule`. The other adapters
use dedicated clients, bypass reusable session caches and configured credential
overrides, reject scripts/unsupported Host options, and require confirmed
resource closure before releasing a tracked session. Redirects and resumable
upload locations cannot send credentials to an unapproved origin.

Plain FTP and Telnet are not eligible. Local files, executable/test/portal
modules have no applicable remote credential. ECauth remains outside per-Host
rotation: its shared control account and Master-issued delegated tokens are
different identities, not the Host password. Managed identities, default
cloud credential providers and S3 AssumeRole are not fallback authentication
paths for a rotated credential.

On the Master and final Movers, each `[PasswordProbeHosts]` value starts with
the module implementation class, followed by that module's endpoint format:

```properties
[PasswordProbeHosts]
42=ecmwf.common.ectrans.module.JSftpModule feed.example.org 22 feed SHA256:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA /probe
43=ecmwf.common.ectrans.module.FtpsModule {"host":"feed.example.org","port":21,"login":"feed","probeDirectory":"/probe","mode":"explicit"}
44=ecmwf.common.ectrans.module.HttpModule {"host":"feed.example.org","port":443,"login":"feed","probeDirectory":"/probe","authentication":"basic"}
45=ecmwf.common.ectrans.module.WebDavModule {"host":"feed.example.org","port":443,"login":"feed","probeDirectory":"/probe","path":"/dav"}
46=ecmwf.common.ectrans.module.AmazonS3Module {"host":"s3.eu-west-1.amazonaws.com","port":443,"login":"existing-host-login","probeDirectory":"/probe","bucket":"feed-bucket","region":"eu-west-1","prefix":"delivery"}
47=ecmwf.common.ectrans.module.AzureModule {"host":"feedaccount.blob.core.windows.net","port":443,"login":"existing-host-login","probeDirectory":"/probe","account":"feedaccount","container":"delivery"}
48=ecmwf.common.ectrans.module.GcsModule {"host":"storage.googleapis.com","port":443,"login":"existing-client-id","probeDirectory":"/probe","project":"feed-project","bucket":"feed-bucket","account":"feed@feed-project.iam.gserviceaccount.com"}
```

For FTPS, use `mode:"implicit"` with the configured implicit-TLS port when
appropriate. HTTPS bearer mode uses `authentication:"bearer"`. WebDAV `path`
defaults to `/`; optional S3/GCS `prefix` defaults to empty. Endpoint login
must match the Host login, even when a replacement cloud key ID changes.
The standard method is
`$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target`;
WebDAV also accepts `$msuser[host]`.

The approved JSON fields are authoritative for rotated connections. Preserve
the intended bucket, prefix and WebDAV base path explicitly when approving an
Host; arbitrary settings from configured-credential connections are not
inherited. WebDAV/GCS
embedded authentication options can be drained, but are never used as fallback
for the replacement. These adapters put only a full-configuration digest in
rotation snapshots, not the embedded option values. The SPI's
`snapshotHostData()` hook must likewise omit embedded credentials when a custom
module admits such options, while continuing to bind every configuration change.

### Credential bundles

Stage a raw password for password modes, or a raw bearer token for HTTPS
bearer mode. Cloud credentials are compact JSON strings in the same encrypted,
one-use handoff:

```json
{"accessKeyId":"REPLACEMENT_ID","secretAccessKey":"REPLACEMENT_SECRET","sessionToken":"OPTIONAL_SESSION_TOKEN"}
```

```json
{"type":"shared_key","accountName":"feedaccount","accountKey":"BASE64_ACCOUNT_KEY"}
```

```json
{"type":"sas","sasToken":"sv=VERSION&spr=https&se=EXPIRY&sp=PERMISSIONS&sr=c&sig=ENCODED_SIGNATURE"}
```

```json
{"type":"access_token","accessToken":"REPLACEMENT_TOKEN","expiresAt":"2030-01-01T00:00:00Z"}
```

The last form applies to Azure and GCS. Omit `sessionToken` for an S3 static
key pair. Azure account-key bundles must match the approved account. Direct
SAS tokens require an explicit future expiry, `spr=https`, and permissions
covering probe write/stat/delete and actual transfers; a remote SAS-fetch API
is not used for a staged replacement. Tokens need more than 60 seconds remaining
when connected; expiring static tokens are not silently refreshed.

For GCS service accounts, stage the service-account JSON containing
`type:"service_account"`, `client_id`, `client_email`, `project_id`,
`private_key_id` and `private_key`. Client ID, email and project must match the
approval. If present, `token_uri` must equal the approved `tokenEndpoint`
(default `https://oauth2.googleapis.com/token`). An explicit alternate token
endpoint must also be verified HTTPS; the Google SDK retains its Google OAuth
JWT audience. Credentials cannot supply a different issuer URL.

All credential strings are limited to **4096 characters**, including JSON
escaping. Larger bundles are rejected, not truncated. The probe creates a
random small object in the approved probe directory/prefix, checks its size,
deletes it and closes the isolated session. It does not create buckets,
containers or directories, nor change cloud lifecycle policies. A failed
cleanup or uncertain closure never passes.

The pin above is illustrative, not a usable server identity. Optional
`[PasswordProbeArchives]` entries use the same Host keys to select the module's
installed archive. Loading uses the normal transfer-module loader. The approved
class/archive must match the Host's declared module, and both are bound into
the endpoint fingerprint. Probe proofs also bind the current method template
and Host options, so changing a protocol or its configuration invalidates them.

Credential rotation is disabled by default. It requires authorization, explicit
endpoint approval, fleet draining and evidence-based retirement.
Use matching OpenECPDS versions on the Master, Monitor and participating Movers.
Hosts that are not enrolled in rotation can continue using their configured
credentials. This workflow stages and switches credentials;
it does not create replacement cloud keys or revoke them at the remote service.
Provider-side revocation remains an operator action after the relevant grace
period. See [Credential rotation enablement](../deployment/kubernetes.md#credential-rotation-enablement)
for configuration, operating constraints and failure handling.

## Related

- [ECtrans Common Options](ectrans.md)
- [OpenECPDS Entities](../concepts/entities.md)
- [Protocols & Connections](../concepts/protocols.md)
- [Plugin Infrastructure](../concepts/plugin-infrastructure.md) — adding new outgoing and incoming protocols
- [Object Storage](../concepts/object-storage.md)
- [Host Directory Field](../host-directory/index.md)
