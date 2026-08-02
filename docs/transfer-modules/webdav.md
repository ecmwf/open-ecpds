# WebDAV Transfer Module

The `webdav` module transfers files to and from remote WebDAV servers (RFC 4918). It supports HTTPS and HTTP, optional write locking, automatic parent-directory creation, and configurable TLS versions.

## Connection

```properties
webdav.scheme = "https"          # https (default) or http
webdav.port = "443"              # remote port
webdav.username = "myuser"       # login (overrides host credentials)
webdav.password = "secret"       # password (overrides host credentials)
webdav.path = "/webdav/data"     # base path prepended to every resource URL
webdav.sslValidation = "false"   # set true to enforce certificate validation
webdav.proxy = "proxy.example.com:3128"  # optional HTTP proxy
```

## Timeouts

```properties
webdav.connectTimeout = "PT30S"   # connection timeout (ISO-8601 duration, default 30 s)
webdav.socketTimeout = "PT300S"   # socket read timeout (default 300 s)
```

## TLS protocols

```properties
webdav.supportedProtocols = "TLSv1.2,TLSv1.3"   # restrict accepted TLS versions
```

## Write locking (RFC 4918)

When `useLock` is enabled, the module sends a `LOCK` request before each upload and `UNLOCK` after, preventing concurrent writes from other clients.

```properties
webdav.useLock = "true"          # enable exclusive write locking
webdav.lockTimeout = "300"       # lock duration in seconds (0 = infinite)
webdav.lockOwner = "ecpds"       # owner string recorded in the lock
```

## Directory creation

```properties
webdav.mkdirs = "true"           # auto-create missing parent directories (default: true)
```

## Full option reference

| Option | Description |
|--------|-------------|
| `webdav.scheme` | URL scheme: `https` (default) or `http` |
| `webdav.port` | Remote port — defaults to 443 (HTTPS) or 80 (HTTP) |
| `webdav.username` | Login for HTTP Basic authentication (overrides host setting) |
| `webdav.password` | Password for HTTP Basic authentication (overrides host setting) |
| `webdav.path` | Base path prepended to every resource URL (default: `/`) |
| `webdav.sslValidation` | When set, validates the server SSL certificate against the trust store |
| `webdav.supportedProtocols` | Comma-separated TLS versions to accept, e.g. `TLSv1.2,TLSv1.3` |
| `webdav.proxy` | HTTP proxy in `host:port` format, e.g. `proxy.example.com:3128` |
| `webdav.connectTimeout` | Connection timeout in seconds (0 = infinite, default: 30) |
| `webdav.socketTimeout` | Read timeout in seconds (0 = infinite, default: 300) |
| `webdav.useLock` | Request an exclusive write lock (RFC 4918) before each upload |
| `webdav.lockTimeout` | Lock duration in seconds (0 = infinite); only used when `webdav.useLock` is set |
| `webdav.lockOwner` | Owner string recorded in the lock request (default: `ecpds`) |
| `webdav.mkdirs` | Create missing parent directories via MKCOL before uploading |
