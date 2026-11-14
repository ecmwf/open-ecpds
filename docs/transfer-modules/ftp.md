# FTP Transfer Module

!!! info
    All options use the `ftp.` prefix. The host *Login* and *Password* fields provide the default credentials but can be overridden with `ftp.login` and `ftp.password`.

## Connection

### Diagnosing stalled uploads

Enable `ectrans.debug = "yes"` on an affected Host and allow DEBUG logging for
`ecmwf.common.ftp.FtpClient` on its Mover. FTP diagnostics include command reply
codes and elapsed times, passive negotiation, control/data connection endpoints,
active-mode fallback, and stream readiness. Command timing records contain the
command verb, not credentials; the existing full dialogue can still contain
sensitive paths or server replies.

The `Ftp timeout on STOR` exception preserves the original socket timeout stack,
distinguishing passive negotiation,
data connection establishment, and command-reply waits.

The no-progress watchdog logs idle time, module status, progress bytes and
available input/output byte counters before cancellation. FTP connection tuples
are also logged without invoking external socket tools. Byte counters represent
application stream progress, not remote TCP acknowledgement.

Failure reporting uses the connected module and preserves statistics already
available before close. It does **not** wait for external diagnostics or guarantee
that late asynchronous close statistics reach the database. Use the passive
Mover-side collector for periodic TCP evidence and correlate by tuple and UTC
time. FTP diagnostics do not change commands, timeouts, retry or close policy.

### Basic connection

The host name / IP and port are taken from the host record. Use the options below to override.

| Option | Default | Description |
|---|---|---|
| `ftp.port` | `21` | Remote FTP port |
| `ftp.login` | *from host* | FTP username |
| `ftp.password` | *from host* | FTP password |
| `ftp.sharedKey` | *none* | Additional per-account DissFTPd HMAC-SHA256 key (64 hexadecimal characters); requires the password as well |
| `ftp.nopassword` | `false` | Authenticate without sending a password (anonymous-style) |
| `ftp.listenAddress` | *none* | Local IP address to bind for data connections |

### DissFTPd shared-key authentication

With an updated DissFTPd server, a host can require proof of a separate shared
key as well as its normal password. Generate a different key for each account
on the server, without printing it:

```bash
(umask 077; set -C; openssl rand -hex 32 > /secure/path/account.key)
```

In DissFTPd's `dissFTP.properties`, configure the account's key file:

```properties
sharedKeyFile.account=/secure/path/account.key
```

The file contains exactly 64 hexadecimal characters, optionally followed by a
newline. Relative paths resolve under the DissFTP configuration directory.
Keep the file readable only by the service account, outside its FTP root, and
not writable by FTP users. Keep a real, nonempty password for this account in
`dissFTP.accounts`; empty passwords and `*` are rejected for key-protected
accounts. Restart DissFTPd after changing its configuration. Key-file contents
are loaded at each new login; existing authenticated connections are not revoked
by changing a key.

Provision the same hexadecimal value securely in the OpenECPDS host's
`ftp.sharedKey` option. It is marked as a secured option, like `ftp.password`.
Do not use the RCC or CLI shared secret. `ftp.nopassword` must remain false.

The client requests a random, connection-bound challenge and returns an
HMAC-SHA256 proof covering the protocol version, account name and challenge.
Challenges expire after 60 seconds and each proof attempt consumes its challenge.
The key is never transmitted. The server requires key verification before
accepting the password, and the client repeats the exchange on reconnect.
Cached connections are not reused across different passwords or keys.

There is **no password-only fallback** when a key is configured: missing,
unreadable or invalid server key files deny login, as do missing or incorrect
client keys. Existing accounts without `sharedKeyFile.<account>` and hosts
without `ftp.sharedKey` retain password-only behaviour. Ordinary FTP clients
cannot access key-protected accounts unless they implement this extension.

**This is authentication, not encryption or two-factor authentication.** Plain
FTP still exposes the password and file contents, and an active attacker can
relay the handshake. Use the `ftp` module over a trusted secure tunnel with
authenticated endpoints. The separate `ftps` module does not implement this
shared-key extension.

### DissFTPd container configuration overrides

The DissFTPd container accepts optional configuration files under
`/etc/ecpds/dissftpd/`. For example, bind-mount a local accounts file read-only:

```bash
--mount type=bind,src=/absolute/path/dissFTP.accounts,dst=/etc/ecpds/dissftpd/dissFTP.accounts,readonly
```

The filename is `dissFTP.accounts` (plural). You can also override
`dissFTP.properties` and `dissFTP.groups` in the same directory, or mount the
whole directory. Each supplied file **replaces** its packaged counterpart; it
is not merged. Files not supplied continue to use the packaged defaults under
`/usr/local/ecpds/dissftpd/conf/`. An empty accounts override allows no accounts;
an existing but unreadable or invalid override does not fall back to defaults.

Keep accounts files and shared keys accessible only to the service account.
Use absolute container paths for `sharedKeyFile.<account>` and mount those key
files read-only too. Relative key paths still resolve under the packaged
configuration directory. Restart the container after changing accounts, groups
or properties. Rebuild the DissFTPd RPM and container image to enable this
override support.

### Data channel mode

| Option | Default | Description |
|---|---|---|
| `ftp.passive` | `no` | `no` = active, `yes` = passive (PASV), `shared` = passive with shared data connection |
| `ftp.extended` | `false` | Use EPSV/EPRT (IPv6-friendly extended passive/active) |
| `ftp.lowPort` | `false` | Use low port numbers (<1024) for active mode data connections |
| `ftp.dataAlive` | `false` | Keep the data connection alive between transfers |

### Timeouts & keep-alive

| Option | Default | Description |
|---|---|---|
| `ftp.commTimeOut` | `1m` | Control connection command timeout |
| `ftp.dataTimeOut` | `1m` | Data connection read/write timeout |
| `ftp.portTimeOut` | `1m` | Timeout waiting for the data port to open |
| `ftp.keepAlive` | `0` | Duration to cache and reuse FTP connections (e.g. `30s`); 0 disables caching |
| `ftp.useNoop` | `0` | Send NOOP commands on idle connections at this interval to keep them alive |
| `ftp.keepControlConnectionAlive` | `false` | Prevent the control connection from timing out during long data transfers |

### Buffer sizes

| Option | Default | Description |
|---|---|---|
| `ftp.sendBuffSize` | *OS default* | TCP send buffer size (e.g. `256k`) |
| `ftp.receiveBuffSize` | *OS default* | TCP receive buffer size |

### NOOP command customisation

| Option | Default | Description |
|---|---|---|
| `ftp.setNoop` | *none* | Custom command to send instead of NOOP (e.g. `STAT`) |

### Quick-start examples

```properties
ftp.passive = "yes"
ftp.port = "21"
ftp.commTimeOut = "2m"
ftp.dataTimeOut = "5m"
```

```properties
ftp.keepAlive = "60s"
ftp.useNoop = "30s"
```

## Transfer

### Temporary & suffix handling

| Option | Default | Description |
|---|---|---|
| `ftp.usetmp` | `true` | Upload to a `.tmp` file and rename on completion |
| `ftp.prefix` | *empty* | Prefix to prepend to the remote filename during upload |
| `ftp.suffix` | *empty* | Suffix to append to the remote filename during upload |
| `ftp.mksuffix` | `false` | Generate a unique suffix automatically |
| `ftp.usesuffix` | `false` | Keep the generated suffix in the final remote name |
| `ftp.useAppend` | `false` | Use APPE (append) instead of STOR for uploads |
| `ftp.deleteOnRename` | `true` | Delete existing destination file before rename |

### Parallelism & integrity

| Option | Default | Description |
|---|---|---|
| `ftp.parallelStreams` | `0` | Number of parallel data streams for GET/PUT (0 = single stream) |
| `ftp.ignoreCheck` | `true` | Skip post-transfer integrity check (SIZE command) |
| `ftp.retryAfterTimeoutOnCheck` | `false` | Retry the integrity check after a timeout instead of failing |
| `ftp.ignoreDelete` | `true` | Ignore errors when deleting remote files |
| `ftp.md5Ext` | `.md5` | Extension used for the checksum sidecar file when `ectrans.checksumAlgorithm` is `MD5` (default) and `ectrans.checksumExt` is not set. For other algorithms, or to override regardless of algorithm, use `ectrans.checksumExt` (see [ECtrans Common Options](ectrans.md)) |

### Directory listing

| Option | Default | Description |
|---|---|---|
| `ftp.usenlist` | `false` | Use NLST instead of LIST for directory listing |
| `ftp.ftpLike` | `false` | When using NLST, reformat entries to look like LIST output |
| `ftp.ftpuser` | *from host login* | Owner user shown in generated directory listings |
| `ftp.ftpgroup` | *from host login* | Owner group shown in generated directory listings |

### Quick-start examples

```properties
ftp.usetmp = "yes"
ftp.ignoreCheck = "no"
ftp.deleteOnRename = "yes"
```

```properties
ftp.parallelStreams = "4"
ftp.passive = "yes"
```

## Directory & Paths

### Working directory

| Option | Default | Description |
|---|---|---|
| `ftp.cwd` | *from host path* | Remote working directory (CWD) to change to after login |
| `ftp.usecleanpath` | `false` | Normalise remote paths (remove double slashes, etc.) |

### Directory creation (mkdirs)

| Option | Default | Description |
|---|---|---|
| `ftp.mkdirs` | `yes` | `yes` = create directories locally before transfer, `no` = never create, `remote` = send MKD commands to the server |
| `ftp.mkdirsCmdIndex` | `0` | Path component depth at which to start creating directories |
| `ftp.ignoreMkdirsCmdErrors` | `false` | Ignore MKD command errors (useful when directory may already exist) |

### Quick-start example

```properties
ftp.mkdirs = "remote"
ftp.ignoreMkdirsCmdErrors = "yes"
ftp.cwd = "/data/incoming"
```

## Hooks

!!! warning
    Hook commands are sent as raw FTP commands. Syntax errors may abort the transfer.

### Connection hooks

| Option | When | Description |
|---|---|---|
| `ftp.postConnectCmd` | after login | FTP command to run immediately after successful login |
| `ftp.preCloseCmd` | before logout | FTP command to run before closing the session |

### PUT hooks

| Option | When | Description |
|---|---|---|
| `ftp.prePutCmd` | before STOR | FTP command sent before uploading a file |
| `ftp.postPutCmd` | after STOR | FTP command sent after uploading a file |

### GET hooks

| Option | When | Description |
|---|---|---|
| `ftp.preGetCmd` | before RETR | FTP command sent before downloading a file |
| `ftp.postGetCmd` | after RETR | FTP command sent after downloading a file |

### Mkdirs hooks

| Option | When | Description |
|---|---|---|
| `ftp.preMkdirsCmd` | before MKD | FTP command sent before creating directories |
| `ftp.postMkdirsCmd` | after MKD | FTP command sent after creating directories |

### Quick-start example

```properties
ftp.postPutCmd = "SITE EXEC /opt/scripts/notify.sh"
```

## Related

- [Transfer Modules overview](index.md)
- [Hosts & Transfer Methods](../concepts/entities.md)
