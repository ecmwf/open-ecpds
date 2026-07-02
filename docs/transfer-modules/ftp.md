# FTP Transfer Module

!!! info
    All options use the `ftp.` prefix. The host *Login* and *Password* fields provide the default credentials but can be overridden with `ftp.login` and `ftp.password`.

## Connection

### Basic connection

The host name / IP and port are taken from the host record. Use the options below to override.

| Option | Default | Description |
|---|---|---|
| `ftp.port` | `21` | Remote FTP port |
| `ftp.login` | *from host* | FTP username |
| `ftp.password` | *from host* | FTP password |
| `ftp.nopassword` | `false` | Authenticate without sending a password (anonymous-style) |
| `ftp.listenAddress` | *none* | Local IP address to bind for data connections |

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
| `ftp.md5Ext` | *none* | If set, write an MD5 sidecar file with this extension (e.g. `.md5`) |

### Directory listing

| Option | Default | Description |
|---|---|---|
| `ftp.usenlist` | `false` | Use NLST instead of LIST for directory listing |
| `ftp.like` | `false` | When using NLST, reformat entries to look like LIST output |
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
