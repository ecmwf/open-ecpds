# FTPS Transfer Module

!!! info
    All options use the `ftps.` prefix. The module supports plain **FTP**, implicit **FTPS** (TLS from the start) and explicit **FTPES** (STARTTLS). Choose the mode with `ftps.connectionType`. Host *Login* and *Password* provide the default credentials but can be overridden with `ftps.login` / `ftps.password`.

## Connection

### Basic connection

| Option | Default | Description |
|---|---|---|
| `ftps.port` | `21` | Remote port (use 990 for implicit FTPS) |
| `ftps.login` | *from host* | FTP username |
| `ftps.password` | *from host* | FTP password |
| `ftps.connectionType` | `FTP` | Protocol variant: `FTP` (plain), `FTPS` (implicit TLS), `FTPES` (explicit TLS / STARTTLS) |
| `ftps.passive` | `false` | Use PASV (passive) mode instead of active |
| `ftps.listenAddress` | *none* | Local IP to bind for data connections |
| `ftps.cwd` | *none* | Initial working directory after login |
| `ftps.connectionTimeOut` | `1m` | Socket connection timeout |
| `ftps.readTimeOut` | `1m` | Socket read timeout |
| `ftps.closeTimeOut` | `1m` | Graceful close timeout |
| `ftps.keepAlive` | `0` | Send keep-alive command every N ms (0 = disabled) |
| `ftps.useNoop` | `0` | Send NOOP command every N ms to keep control channel alive (0 = disabled) |
| `ftps.sendBuffSize` | *OS default* | TCP send buffer size (e.g. `256KB`) |
| `ftps.receiveBuffSize` | *OS default* | TCP receive buffer size |

## TLS / Security

### TLS settings

Only relevant when `ftps.connectionType` is `FTPS` or `FTPES`.

| Option | Default | Description |
|---|---|---|
| `ftps.protocol` | `TLS` | SSL/TLS protocol name passed to `SSLContext.getInstance()` (e.g. `TLSv1.2`, `TLSv1.3`) |
| `ftps.strict` | `false` | When `false` all server certificates are accepted (trust-all). Set to `true` to use the JVM default trust store for strict certificate validation. |

!!! warning
    Leaving `ftps.strict=false` (the default) disables certificate validation. Use `true` in production environments to prevent man-in-the-middle attacks.

## Transfer

### File transfer behaviour

| Option | Default | Description |
|---|---|---|
| `ftps.usetmp` | `true` | Upload to a temporary name then rename on completion |
| `ftps.useAppend` | `false` | Use APPE (append) instead of STOR for resume support |
| `ftps.mkdirs` | `true` | Create remote directory hierarchy if it does not exist |
| `ftps.prefix` | *none* | Prefix added to the remote filename |
| `ftps.suffix` | *none* | Suffix added to the remote filename |
| `ftps.mksuffix` | `false` | Generate a random suffix and append it to the filename |
| `ftps.usesuffix` | `false` | Use the suffix set by `ftps.suffix` during upload, rename away on completion |
| `ftps.usecleanpath` | `false` | Strip duplicate slashes and resolve `.`/`..` in paths before sending |
| `ftps.deleteOnRename` | `true` | Delete the target before renaming the temporary file (avoids rename-to-existing failures) |
| `ftps.ignoreCheck` | `true` | Do not verify the transferred file size after upload |
| `ftps.ignoreDelete` | `true` | Ignore errors when deleting the remote file |
| `ftps.md5Ext` | `.md5` | Extension used for MD5 checksum sidecar files |

## Paths

### Directory creation

| Option | Default | Description |
|---|---|---|
| `ftps.mkdirsCmdIndex` | `0` | Path component depth at which to start creating directories (0 = from root) |
| `ftps.preMkdirsCmd` | *none* | FTP command(s) sent before creating directories (newline-separated) |
| `ftps.postMkdirsCmd` | *none* | FTP command(s) sent after creating directories |
| `ftps.ignoreMkdirsCmdErrors` | `false` | Continue even if pre/post mkdir commands return an error |

## Hooks

### FTP command hooks

Each option accepts one or more raw FTP commands separated by newlines. Commands are sent over the control channel at the indicated lifecycle point.

| Option | When |
|---|---|
| `ftps.postConnectCmd` | Immediately after login |
| `ftps.preCloseCmd` | Before closing the control connection |
| `ftps.prePutCmd` | Before each file upload |
| `ftps.postPutCmd` | After each successful upload |
| `ftps.preGetCmd` | Before each file download |
| `ftps.postGetCmd` | After each successful download |

### Example

```properties
ftps.connectionType = "FTPES"
ftps.passive = "true"
ftps.usetmp = "true"
ftps.mkdirs = "true"
ftps.postConnectCmd = "OPTS UTF8 ON"
```

## Related

- [Transfer Modules overview](index.md)
- [Hosts & Transfer Methods](../concepts/entities.md)
