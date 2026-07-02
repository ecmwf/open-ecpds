# ECauth (SSH/Telnet) Transfer Module

!!! info
    The **ECauth** module transfers files over an interactive **SSH** or **Telnet** session authenticated through ECMWF's ECauth service. All options use the `ecauth.` prefix. Credentials default to the ECauth service account but can be overridden with `ecauth.user` / `ecauth.pass`.

## Connection

### Basic connection

| Option | Default | Description |
| --- | --- | --- |
| `ecauth.protocol` | `ssh` | Transport protocol: `ssh` (port 22) or `telnet` (port 23) |
| `ecauth.port` | `22` / `23` | Remote port. Defaults to 22 for SSH, 23 for Telnet. Override here if non-standard. |
| `ecauth.user` | *from config* | ECauth service username used to open the interactive session |
| `ecauth.pass` | *from config* | ECauth service password |
| `ecauth.hostList` | *none* | Comma-separated list of target hostnames. The module picks one at runtime (optionally with load balancing). |
| `ecauth.resolveIP` | `true` | Resolve hostnames to IP addresses before connecting |
| `ecauth.proxyList` | *none* | Comma-separated list of SOCKS/proxy addresses to route through |
| `ecauth.listenAddress` | *none* | Local IP address to bind when opening data connections |
| `ecauth.cwd` | *from host* | Initial working directory after login. Defaults to the user's home directory from the ECauth session. |
| `ecauth.connectTimeOut` | `30s` | TCP connection timeout |
| `ecauth.sessionTimeOut` | *none* | Maximum lifetime of an interactive session before it is recycled |
| `ecauth.keepAlive` | `0` | Send keep-alive command every N ms (0 = disabled) |
| `ecauth.useNoop` | `0` | Send NOOP every N ms to keep the session alive (0 = disabled) |

## SSH / Auth

### SSH authentication

Only relevant when `ecauth.protocol=ssh`.

| Option | Default | Description |
| --- | --- | --- |
| `ecauth.privateKeyFile` | *none* | Path to a PEM private key file on the DataMover for public-key authentication |
| `ecauth.privateKey` | *none* | Inline PEM private key (alternative to `privateKeyFile`) |
| `ecauth.passPhrase` | *none* | Passphrase for the private key if it is encrypted |
| `ecauth.fingerPrint` | *none* | Expected SSH host key fingerprint. Connection is refused if the server fingerprint does not match. |
| `ecauth.cipher` | `none` | SSH cipher suite to negotiate (e.g. `aes128-ctr`). `none` lets JSch negotiate automatically. |
| `ecauth.compression` | `none` | SSH compression algorithm (e.g. `zlib`). `none` disables compression. |
| `ecauth.serverAliveInterval` | *none* | SSH server-alive message interval (e.g. `60s`). Prevents idle disconnection. |
| `ecauth.serverAliveCountMax` | *none* | Number of unanswered server-alive messages before the session is considered dead |

## Transfer

### File transfer behaviour

| Option | Default | Description |
| --- | --- | --- |
| `ecauth.usetmp` | `false` | Upload to a temporary name (with suffix) then rename on completion |
| `ecauth.mkdirs` | `false` | Create remote directory hierarchy if it does not exist |
| `ecauth.prefix` | *none* | Prefix added to the remote filename |
| `ecauth.suffix` | `.tmp` | Suffix used during upload when `usetmp=true` (default is `.tmp` when no explicit prefix/suffix is set) |
| `ecauth.mksuffix` | `false` | Generate a random 3-character suffix instead of using `ecauth.suffix` |
| `ecauth.ignoreCheck` | `true` | Skip the post-upload file size verification |
| `ecauth.usemget` | `false` | Use `mget` for bulk retrieval operations instead of individual `get` calls |
| `ecauth.listOptions` | *none* | Extra options appended to the remote `ls` / directory listing command |
| `ecauth.copyCmd` | *none* | Custom shell command template for server-side copy operations |
| `ecauth.chmodOnCopy` | `640` | Octal permission mode applied to files after a server-side copy |

### Exec hook

| Option | Default | Description |
| --- | --- | --- |
| `ecauth.execCmd` | *none* | Shell command executed on the remote host after each transfer |
| `ecauth.execCode` | `0` | Expected exit code of `execCmd`. Any other exit code is treated as an error. |

## Advanced

### Directory creation hooks

| Option | Default | Description |
| --- | --- | --- |
| `ecauth.mkdirsCmdIndex` | `0` | Path component depth at which to start issuing mkdir commands (0 = from root) |
| `ecauth.preMkdirsCmd` | *none* | Shell command(s) executed before creating directories |
| `ecauth.postMkdirsCmd` | *none* | Shell command(s) executed after creating directories |
| `ecauth.ignoreMkdirsCmdErrors` | `false` | Continue even if pre/post mkdir commands return a non-zero exit code |

### Example

```properties
ecauth.protocol = "ssh"
ecauth.port = "22"
ecauth.usetmp = "true"
ecauth.mkdirs = "true"
ecauth.ignoreCheck = "false"
ecauth.serverAliveInterval = "60s"
ecauth.serverAliveCountMax = "3"
ecauth.chmodOnCopy = "644"
```

## Related

- [Transfer Modules overview](index.md)
