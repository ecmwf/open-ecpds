# SFTP Transfer Module

!!! info
    All options use the `sftp.` prefix. The host *Login* and *Password* fields provide default credentials but can be overridden. Key-based authentication is also supported via `sftp.privateKey` or `sftp.privateKeyFile`.

## Connection

### Basic connection

| Option | Default | Description |
|---|---|---|
| `sftp.port` | `22` | Remote SSH/SFTP port |
| `sftp.login` | *from host* | SSH username |
| `sftp.password` | *from host* | SSH password (used if no private key is set) |
| `sftp.listenAddress` | *none* | Local IP address to bind for outgoing connections |

### Timeouts & keep-alive

| Option | Default | Description |
|---|---|---|
| `sftp.sessionTimeOut` | `1m` | SSH session idle timeout |
| `sftp.connectTimeOut` | `30s` | TCP connection timeout |
| `sftp.serverAliveInterval` | *none* | Interval for SSH keep-alive packets (e.g. `30s`) |
| `sftp.serverAliveCountMax` | `0` | Max unanswered keep-alive packets before disconnecting |

### Crypto & algorithm negotiation

| Option | Default | Description |
|---|---|---|
| `sftp.kex` | `default` | Key-exchange algorithms (comma-separated JSch names) |
| `sftp.cipher` | `none` | Cipher algorithms (e.g. `aes128-ctr,aes256-ctr`) |
| `sftp.mac` | *JSch default* | MAC algorithms (e.g. `hmac-sha2-256`) |
| `sftp.compression` | `none` | Compression: `none` or `zlib@openssh.com` |
| `sftp.serverHostKey` | *JSch default* | Accepted server host key types (e.g. `ssh-ed25519`) |
| `sftp.clientVersion` | *JSch default* | Custom SSH client version string |

### JSch properties & options

| Option | Description |
|---|---|
| `sftp.options` | Raw JSch option string (key=value pairs, semicolon-separated) |
| `sftp.properties` | Additional JSch properties as key=value pairs |

### Quick-start examples

```properties
sftp.port = "22"
sftp.sessionTimeOut = "5m"
sftp.serverAliveInterval = "30s"
sftp.serverAliveCountMax = "3"
```

```properties
sftp.kex = "ecdh-sha2-nistp521,ecdh-sha2-nistp384"
sftp.cipher = "aes256-ctr"
sftp.mac = "hmac-sha2-256"
```

## Auth & Keys

### Key-based authentication

Provide either a file path or the inline key. Inline keys take precedence over `sftp.privateKeyFile`. The key must be in PEM (OpenSSH or RSA) format.

| Option | Default | Description |
|---|---|---|
| `sftp.privateKey` | *empty* | Inline PEM private key (paste the full key block) |
| `sftp.privateKeyFile` | *empty* | Path to PEM private key file on the ECpds host |
| `sftp.passPhrase` | *none* | Passphrase for the private key (if encrypted) |

### Host key verification

| Option | Default | Description |
|---|---|---|
| `sftp.fingerPrint` | *none* | Expected server fingerprint (SHA-256 or MD5 hex). If set, the connection fails unless the server key matches. |

!!! warning
    If `sftp.fingerPrint` is not set, host key checking depends on the JSch `StrictHostKeyChecking` property. Default is `no` (accept any host key). Set it via `sftp.options` for production environments.

### Authentication methods

| Option | Default | Description |
|---|---|---|
| `sftp.preferredAuthentications` | *JSch default* | Ordered list of auth methods (e.g. `publickey,password`) |

### Quick-start examples

```properties
sftp.privateKeyFile = "/opt/ecpds/.ssh/id_ed25519"
sftp.preferredAuthentications = "publickey"
```

```properties
sftp.privateKey = "-----BEGIN OPENSSH PRIVATE KEY-----
b3Blb...
-----END OPENSSH PRIVATE KEY-----"
sftp.fingerPrint = "SHA256:abc123..."
```

## Transfer

### Temporary & suffix handling

| Option | Default | Description |
|---|---|---|
| `sftp.usetmp` | `true` | Upload to a `.tmp` file and rename on completion |
| `sftp.prefix` | *empty* | Prefix to prepend to the remote filename during upload |
| `sftp.suffix` | *empty* | Suffix to append to the remote filename during upload |
| `sftp.mksuffix` | `false` | Generate a unique suffix automatically |
| `sftp.chmod` | *none* | Octal permission mask to apply after upload (e.g. `644`) |

### Performance & integrity

| Option | Default | Description |
|---|---|---|
| `sftp.bulkRequestNumber` | `64` | SFTP read-ahead / in-flight request count (higher = faster on high-latency links) |
| `sftp.useWriteFlush` | `false` | Apply write-flush workaround for some server implementations |
| `sftp.ignoreCheck` | `false` | Skip post-transfer size verification |
| `sftp.md5Ext` | *none* | If set, write an MD5 sidecar file with this extension |

### Post-upload command

Execute a remote shell command after the upload completes. The command is run via the SSH channel.

| Option | Default | Description |
|---|---|---|
| `sftp.execCmd` | *none* | Remote shell command to execute after a successful upload |
| `sftp.execCode` | `0` | Expected exit code; non-matching exits cause the transfer to fail |

### Quick-start examples

```properties
sftp.usetmp = "yes"
sftp.chmod = "644"
sftp.bulkRequestNumber = "128"
```

```properties
sftp.execCmd = "/opt/scripts/notify.sh {filename}"
sftp.execCode = "0"
```

## Directory & Listing

### Working directory

| Option | Default | Description |
|---|---|---|
| `sftp.cwd` | *from host path* | Remote working directory to change to after login |
| `sftp.usecleanpath` | `false` | Normalise remote paths (remove double slashes, etc.) |

### Directory creation (mkdirs)

| Option | Default | Description |
|---|---|---|
| `sftp.mkdirs` | `true` | Automatically create parent directories on the remote host |
| `sftp.mkdirsCmdIndex` | `0` | Path component depth at which to start creating directories |
| `sftp.ignoreMkdirsCmdErrors` | `false` | Ignore mkdir errors (useful when directory may already exist) |
| `sftp.preMkdirsCmd` | *none* | Shell command to run before creating directories |
| `sftp.postMkdirsCmd` | *none* | Shell command to run after creating directories |

### Listing / acquisition

These options control how the SFTP module traverses remote directories during an acquisition (listing) run.

| Option | Default | Description |
|---|---|---|
| `sftp.listRecursive` | `true` | Recursively list subdirectories |
| `sftp.listMaxThreads` | `10` | Maximum concurrent threads for parallel directory listing |
| `sftp.listMaxWaiting` | `100` | Maximum number of pending listing tasks in the queue |
| `sftp.listMaxDirs` | `50000` | Abort listing if more than this many directories are encountered |

### Quick-start examples

```properties
sftp.listRecursive = "yes"
sftp.listMaxThreads = "20"
sftp.listMaxWaiting = "500"
sftp.listMaxDirs = "100000"
```

```properties
sftp.mkdirs = "yes"
sftp.ignoreMkdirsCmdErrors = "yes"
sftp.cwd = "/data/incoming"
```

## Related

- [Transfer Modules overview](index.md)
- [Hosts & Transfer Methods](../concepts/entities.md)
